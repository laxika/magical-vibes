package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IngeniousSkaab.class, Shock.class, GrizzlyBears.class})
class IngeniousSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess gives Ingenious Skaab +1/+1 after casting a noncreature spell")
    void prowessBoostsAfterCastingNoncreatureSpell() {
        Permanent skaab = addReadySkaab();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(skaab.getPowerModifier()).isEqualTo(1);
        assertThat(skaab.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prowess does not trigger from casting a creature spell")
    void prowessDoesNotTriggerForCreatureSpell() {
        Permanent skaab = addReadySkaab();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(skaab.getPowerModifier()).isEqualTo(0);
        assertThat(skaab.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The blue ability gives Ingenious Skaab +1/-1 until end of turn")
    void blueAbilityBoostsUntilEndOfTurn() {
        Permanent skaab = addReadySkaab();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skaab.getPowerModifier()).isEqualTo(1);
        assertThat(skaab.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skaab.getPowerModifier()).isEqualTo(0);
        assertThat(skaab.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Prowess resolves before its triggering spell and expires at end of turn")
    void prowessResolvesBeforeSpellAndExpires() {
        Permanent skaab = addReadySkaab();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(skaab.getPowerModifier()).isZero();
        assertThat(skaab.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(skaab.getPowerModifier()).isEqualTo(1);
        assertThat(skaab.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(skaab.getPowerModifier()).isZero();
        assertThat(skaab.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each noncreature spell gives a separate prowess boost")
    void prowessStacksForMultipleSpells() {
        Permanent skaab = addReadySkaab();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (int i = 0; i < 2; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }

        assertThat(skaab.getPowerModifier()).isEqualTo(2);
        assertThat(skaab.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentsSpellDoesNotTriggerProwess() {
        Permanent skaab = addReadySkaab();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(skaab.getPowerModifier()).isZero();
        assertThat(skaab.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The blue ability can be activated repeatedly while summoning sick and tapped")
    void blueAbilityStacksWithoutTapOrHasteRequirement() {
        Permanent skaab = addReadySkaab();
        skaab.setSummoningSick(true);
        skaab.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(skaab.getPowerModifier()).isEqualTo(2);
        assertThat(skaab.getToughnessModifier()).isEqualTo(-2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skaab);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three blue activations put Ingenious Skaab into the graveyard for zero toughness")
    void blueAbilityCanReduceToughnessToZero() {
        addReadySkaab();
        harness.addMana(player1, ManaColor.BLUE, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .allMatch(card -> card instanceof IngeniousSkaab);
    }

    private Permanent addReadySkaab() {
        Permanent skaab = addCreatureReady(player1, new IngeniousSkaab());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return skaab;
    }
}
