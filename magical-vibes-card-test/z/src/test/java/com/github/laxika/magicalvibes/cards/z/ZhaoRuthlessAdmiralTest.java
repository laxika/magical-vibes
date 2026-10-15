package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MistyPalmsOasis;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZhaoRuthlessAdmiral.class, DiabolicEdict.class, GrizzlyBears.class, MistyPalmsOasis.class})
class ZhaoRuthlessAdmiralTest extends BaseCardTest {

    @Test
    void attackingAddsTwoRedManaUntilEndOfCombat() {
        addReadyZhao();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void sacrificingAnotherPermanentBoostsYourCreaturesUntilEndOfTurn() {
        addReadyZhao();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        sacrificeAt(player1, bears);

        assertThat(gqs.getEffectivePower(gd, gd.playerBattlefields.get(player1.getId()).getFirst()))
                .isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gd.playerBattlefields.get(player1.getId()).getFirst()))
                .isEqualTo(3);
    }

    @Test
    void sacrificingZhaoDoesNotTriggerTheAbility() {
        Permanent zhao = addReadyZhao();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        sacrificeAt(player1, zhao);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    void sacrificingAnOpponentsCreatureDoesNotBoostZhao() {
        Permanent zhao = addReadyZhao();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        sacrificeAt(player2, bears);

        assertThat(gqs.getEffectivePower(gd, zhao)).isEqualTo(3);
    }

    @Test
    void eachSacrificeBoostsAllYourExistingCreaturesButNotOpposingCreatures() {
        Permanent zhao = addReadyZhao();
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        sacrificeAt(player1, first);
        sacrificeAt(player1, second);

        assertThat(gqs.getEffectivePower(gd, zhao)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, zhao)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
    }

    @Test
    void creaturesArrivingAfterTheTriggerResolvesDoNotReceiveTheBoost() {
        Permanent zhao = addReadyZhao();
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        sacrificeAt(player1, sacrificed);
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, zhao)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
    }

    @Test
    void sacrificingALandAsAnActivationCostAlsoTriggersTheBoost() {
        Permanent zhao = addReadyZhao();
        harness.addToBattlefield(player1, new MistyPalmsOasis());
        harness.setLibrary(player1, List.of(new ZhaoRuthlessAdmiral()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, 2, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Misty Palms Oasis");
        assertThat(gqs.getEffectivePower(gd, zhao)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zhao)).isEqualTo(4);
    }

    private Permanent addReadyZhao() {
        return addCreatureReady(player1, new ZhaoRuthlessAdmiral());
    }

    private void sacrificeAt(Player player, Permanent sacrificed) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, player.getId());
        harness.handlePermanentChosen(player, sacrificed.getId());
        harness.passBothPriorities();
    }
}
