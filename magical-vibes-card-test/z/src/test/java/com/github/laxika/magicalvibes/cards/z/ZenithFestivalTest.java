package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZenithFestival.class, Forest.class, GrizzlyBears.class})
class ZenithFestivalTest extends BaseCardTest {

    @Test
    void exilesTopXCardsAndGrantsPlayPermission() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        Card remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, remaining));
        harness.setHand(player1, List.of(new ZenithFestival()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second, third);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    void harmonizeCastsWithXAndCreaturePowerReduction() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ZenithFestival spell = new ZenithFestival();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playFlashbackSpell(gd, player1, 0, 2, null, List.of(), List.of(), null,
                List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    void zeroXLeavesLibraryUntouchedAndSpellGoesToGraveyard() {
        Card top = new Forest();
        ZenithFestival spell = new ZenithFestival();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    void exilesOnlyAvailableCardsWhenXExceedsLibrarySize() {
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new ZenithFestival()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
    }

    @Test
    void canPlayExiledLandButCannotExceedNormalLandLimit() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new ZenithFestival()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 2);

        gs.playCardFromExile(gd, player1, first.getId(), null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()));
        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, second.getId(), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void exiledCreatureRequiresItsNormalManaCost() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new ZenithFestival()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 1);

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, creature.getId(), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);

        harness.addMana(player1, ManaColor.GREEN, 2);
        gs.playCardFromExile(gd, player1, creature.getId(), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void permissionLastsThroughNextTurnAndUnusedCardRemainsExiledAfterExpiry() {
        Card exiled = new Forest();
        harness.setLibrary(player1, List.of(exiled, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ZenithFestival()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, exiled.getId(), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void harmonizeCanBePaidWithoutTappingACreature() {
        Card top = new Forest();
        ZenithFestival spell = new ZenithFestival();
        harness.setLibrary(player1, List.of(top));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFlashback(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top, spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId())
                .doesNotContainKey(spell.getId());
    }

    @Test
    void harmonizePowerReductionCannotPayColoredMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ZenithFestival spell = new ZenithFestival();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, 1, null,
                List.of(), List.of(), null, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }
}
