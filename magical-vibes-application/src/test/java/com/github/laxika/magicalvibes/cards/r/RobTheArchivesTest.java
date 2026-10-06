package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RobTheArchives.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class RobTheArchivesTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top two cards and grants play permission until end of turn")
    void exilesTopTwoCardsForPlayThisTurn() {
        Card first = new Forest();
        Card second = new Forest();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setHand(player1, List.of(new RobTheArchives()));
        addMana();

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Casualty copies the spell and exiles two cards for each resolution")
    void casualtyCopiesSpell() {
        Permanent casualtyCreature = addCreatureReady(player1, new GrizzlyBears());
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, remaining));
        harness.setHand(player1, List.of(new RobTheArchives()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, casualtyCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId(), third.getId(), fourth.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    @Test
    @DisplayName("Cannot pay casualty with a creature below the required power")
    void rejectsUnderpoweredCasualtyCreature() {
        Permanent casualtyCreature = addCreatureReady(player1, new Ornithopter());
        harness.setHand(player1, List.of(new RobTheArchives()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, casualtyCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 1");
    }

    @Test
    void exiledSpellRequiresItsNormalManaCost() {
        Card spell = new RobTheArchives();
        harness.setLibrary(player1, List.of(spell, new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new RobTheArchives()));
        addMana();
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);

        addMana();
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void exiledLandsStillUseTheNormalLandLimit() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RobTheArchives()));
        addMana();
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.castFromExile(player1, first.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(first.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void permissionExpiresAfterTheCurrentTurn() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land, new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new RobTheArchives()));
        addMana();
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsKey(land.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
    }

    @Test
    void oneCardLibraryExilesOnlyTheAvailableCard() {
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new RobTheArchives()));
        addMana();
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.exilePlayPermissions).containsEntry(onlyCard.getId(), player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void emptyLibraryDoesNotCauseADrawLoss() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new RobTheArchives()));
        addMana();
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anySatisfy(card -> assertThat(card).isInstanceOf(RobTheArchives.class));
    }
    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
