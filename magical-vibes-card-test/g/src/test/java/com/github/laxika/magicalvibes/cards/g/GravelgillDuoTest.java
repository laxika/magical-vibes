package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
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

@CardUsed({GravelgillDuo.class, FugitiveWizard.class, ScatheZombies.class, GrizzlyBears.class})
class GravelgillDuoTest extends BaseCardTest {

    private Permanent addDuo() {
        Permanent duo = harness.addToBattlefieldAndReturn(player1, new GravelgillDuo());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return duo;
    }

    @Test
    @DisplayName("Gets +1/+1 when you cast a blue spell")
    void pumpsOnBlueSpell() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);

        // Cast trigger sits above the creature spell; resolve it.
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains fear when you cast a black spell")
    void gainsFearOnBlackSpell() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("No trigger when you cast a spell that is neither blue nor black")
    void noTriggerOnGreenSpell() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Only the creature spell is on the stack — no cast trigger.
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Blue-spell boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Black-spell fear wears off at end of turn")
    void fearWearsOffAtEndOfTurn() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FEAR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("A blue-black hybrid spell triggers both abilities even when paid with black mana")
    void hybridSpellTriggersBothAbilities() {
        Permanent duo = addDuo();
        harness.setHand(player1, List.of(new GravelgillDuo()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FEAR)).isTrue();

        harness.passBothPriorities();

        Permanent newDuo = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(newDuo).isNotSameAs(duo);
        assertThat(gqs.getEffectivePower(gd, newDuo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newDuo)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, newDuo, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Each blue spell adds another boost until end of turn")
    void blueSpellBoostsAccumulate() {
        Permanent duo = addDuo();
        harness.setHand(player1, List.of(new FugitiveWizard(), new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("An opponent's blue-black spell does not trigger either ability")
    void opponentSpellDoesNotTrigger() {
        Permanent duo = addDuo();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GravelgillDuo()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Gravelgill Duo does not trigger its own abilities when cast")
    void doesNotTriggerOnItsOwnCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GravelgillDuo()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        Permanent duo = findPermanent(player1, "Gravelgill Duo");
        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FEAR)).isFalse();
    }
}
