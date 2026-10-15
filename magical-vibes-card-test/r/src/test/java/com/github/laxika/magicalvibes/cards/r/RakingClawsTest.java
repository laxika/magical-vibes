package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.s.SleeperDart;
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

@CardUsed({RakingClaws.class, SleeperDart.class, AlmightyBrushwagg.class})
class RakingClawsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Raking Claws grants double strike to target creature")
    void resolvingGrantsDoubleStrike() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new RakingClaws()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike granted by Raking Claws wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new RakingClaws()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Raking Claws cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SleeperDart());
        harness.setHand(player1, List.of(new RakingClaws()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cycling Raking Claws discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RakingClaws()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Raking Claws");
        harness.assertInHand(player1, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Raking Claws can target an opponent's creature without affecting other creatures")
    void grantsDoubleStrikeOnlyToOpponentsTargetedCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new RakingClaws()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The targeted creature deals both first-strike and regular combat damage")
    void doubleStrikeDealsDamageTwice() {
        Permanent attacker = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RakingClaws()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cycling discards Raking Claws as a cost before drawing on resolution")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new RakingClaws()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Raking Claws");
        harness.assertNotInHand(player1, "Raking Claws");
        harness.assertNotInHand(player1, "Almighty Brushwagg");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Almighty Brushwagg");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling requires two mana and does not discard the card when payment fails")
    void cyclingCannotBeActivatedWithInsufficientMana() {
        harness.setHand(player1, List.of(new RakingClaws()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertInHand(player1, "Raking Claws");
        harness.assertNotInGraveyard(player1, "Raking Claws");
        harness.assertNotInHand(player1, "Almighty Brushwagg");
        assertThat(gd.stack).isEmpty();
    }
}
