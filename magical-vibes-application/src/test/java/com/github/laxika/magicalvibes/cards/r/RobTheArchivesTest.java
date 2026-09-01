package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RobTheArchives.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
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
        Permanent casualtyCreature = addCreatureReady(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new RobTheArchives()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, casualtyCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 1");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
