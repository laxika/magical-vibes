package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeilstoneAmulet.class, GrizzlyBears.class, GiantGrowth.class, RoyalAssassin.class})
class VeilstoneAmuletTest extends BaseCardTest {

    @Test
    void opponentsCannotTargetYourCreaturesWithSpellsOrAbilities() {
        Permanent creature = resolveTriggerForCreature();

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent assassin = harness.addToBattlefieldAndReturn(player2, new RoyalAssassin());
        assassin.setSummoningSick(false);
        creature.tap();
        int assassinIndex = gd.playerBattlefields.get(player2.getId()).indexOf(assassin);

        assertThatThrownBy(() -> harness.activateAbility(player2, assassinIndex, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controllerCanStillTargetTheirOwnCreature() {
        Permanent creature = resolveTriggerForCreature();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, creature.getId());

        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    void creaturesEnteringLaterAreAffected() {
        resolveTriggerForCreature();
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, laterCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictionExpiresAtEndOfTurn() {
        Permanent creature = resolveTriggerForCreature();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, creature.getId());

        assertThat(gd.stack).isNotEmpty();
    }

    private Permanent resolveTriggerForCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new VeilstoneAmulet());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return creature;
    }
}
