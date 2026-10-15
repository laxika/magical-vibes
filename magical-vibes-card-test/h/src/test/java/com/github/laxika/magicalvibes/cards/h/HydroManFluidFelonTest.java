package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HydroManFluidFelon.class, Opt.class, Shock.class})
class HydroManFluidFelonTest extends BaseCardTest {

    @Test
    void blueSpellPumpsHydroManAndOtherColorsDoNot() {
        Permanent hydroMan = addHydroMan();
        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hydroMan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hydroMan)).isEqualTo(3);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hydroMan)).isEqualTo(3);
    }

    @Test
    void endStepUntapsHydroManMakesItOnlyALandAndGrantsManaUntilNextTurn() {
        Permanent hydroMan = addHydroMan();
        hydroMan.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(hydroMan.isTapped()).isFalse();
        assertThat(gqs.isLand(gd, hydroMan)).isTrue();
        assertThat(gqs.isCreature(gd, hydroMan)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gqs.isLand(gd, hydroMan)).isFalse();
        assertThat(gqs.isCreature(gd, hydroMan)).isTrue();
    }

    @Test
    void opponentsBlueSpellDoesNotPumpHydroMan() {
        Permanent hydroMan = addHydroMan();

        harness.castFromHand(player2, new Opt(), "{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, hydroMan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hydroMan)).isEqualTo(2);
    }

    @Test
    void eachBlueSpellAddsAnotherPump() {
        Permanent hydroMan = addHydroMan();

        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hydroMan)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hydroMan)).isEqualTo(4);
    }

    @Test
    void blueSpellDoesNotTriggerPumpWhileHydroManIsALand() {
        Permanent hydroMan = addHydroMan();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hydroMan)).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.castFromHand(player1, new Opt(), "{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isLand(gd, hydroMan)).isTrue();
    }

    @Test
    void opponentsEndStepDoesNotUntapOrTransformHydroMan() {
        Permanent hydroMan = addHydroMan();
        hydroMan.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(hydroMan.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, hydroMan)).isTrue();
        assertThat(gqs.isLand(gd, hydroMan)).isFalse();
    }

    @Test
    @CardUsed({Confiscate.class})
    void manaAbilityExpiresOnOriginalControllersTurnAfterControlChanges() {
        Permanent hydroMan = addHydroMan();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new Confiscate()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castEnchantment(player2, 0, hydroMan.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hydroMan);

        // Isolate the existing duration by skipping the new controller's end-step trigger.
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gqs.isCreature(gd, hydroMan)).isTrue();
        assertThat(gqs.isLand(gd, hydroMan)).isFalse();
        int permanentIndex = gd.playerBattlefields.get(player2.getId()).indexOf(hydroMan);
        assertThatThrownBy(() -> harness.activateAbility(player2, permanentIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Permanent has no activated ability");
    }

    private Permanent addHydroMan() {
        return addCreatureReady(player1, new HydroManFluidFelon());
    }
}
