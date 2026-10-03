package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CracklingCyclops.class, GrizzlyBears.class, Shock.class})
class CracklingCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell gives Crackling Cyclops +3/+0 until end of turn")
    void noncreatureSpellPumps() {
        Permanent cyclops = addCyclops();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a creature spell does not pump Crackling Cyclops")
    void creatureSpellDoesNotPump() {
        Permanent cyclops = addCyclops();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent cyclops = addCyclops();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent casting a noncreature spell does not pump Crackling Cyclops")
    void opponentNoncreatureSpellDoesNotPump() {
        Permanent cyclops = addCyclops();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each noncreature spell adds another boost before that spell resolves")
    void repeatedCastsStackBoosts() {
        Permanent cyclops = addCyclops();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("The controller's noncreature spell triggers during an opponent's turn")
    void controllerSpellOnOpponentTurnPumps() {
        Permanent cyclops = addCyclops();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each Cyclops boosts only itself when its controller casts a spell")
    void multipleCyclopsBoostTheirOwnSources() {
        Permanent first = addCyclops();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CracklingCyclops());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CracklingCyclops());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(0);
    }

    private Permanent addCyclops() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new CracklingCyclops());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return cyclops;
    }
}
