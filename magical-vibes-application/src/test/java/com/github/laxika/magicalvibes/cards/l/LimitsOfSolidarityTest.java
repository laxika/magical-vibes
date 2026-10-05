package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.o.OketrasMonument;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LimitsOfSolidarity.class, DuneBeetle.class, OketrasMonument.class})
class LimitsOfSolidarityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving untaps target, gains control, and grants haste")
    void resolvesUntapGainControlAndHaste() {
        Permanent target = addCreatureReady(player2, new DuneBeetle());
        target.tap();
        harness.setHand(player1, List.of(new LimitsOfSolidarity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new DuneBeetle());
        harness.setHand(player1, List.of(new LimitsOfSolidarity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new DuneBeetle()); // valid target so spell is playable
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new OketrasMonument());
        harness.setHand(player1, List.of(new LimitsOfSolidarity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cycling discards Limits of Solidarity and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new LimitsOfSolidarity()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Limits of Solidarity");
        harness.assertInHand(player1, "Dune Beetle");
    }

    @Test
    @DisplayName("An own creature can be targeted, untapped, and granted haste")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        target.tap();
        harness.setHand(player1, List.of(new LimitsOfSolidarity()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Dune Beetle");
        declareAttackers(List.of(0));
        assertThat(target.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cycling pays the discard cost before drawing, without a creature target")
    void cyclingDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new LimitsOfSolidarity()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Limits of Solidarity");
        harness.assertNotInHand(player1, "Limits of Solidarity");
        harness.assertNotInHand(player1, "Dune Beetle");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Dune Beetle");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new LimitsOfSolidarity()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Limits of Solidarity");
        harness.assertNotInGraveyard(player1, "Limits of Solidarity");
        harness.assertNotInHand(player1, "Dune Beetle");
        assertThat(gd.stack).isEmpty();
    }
}
