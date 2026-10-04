package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SearingWind;
import com.github.laxika.magicalvibes.cards.v.VintaraSnapper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlitteringLynx.class, SearingWind.class, VintaraSnapper.class})
class GlitteringLynxTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage that would be dealt to it")
    void preventsAllDamageToIt() {
        Permanent lynx = addCreatureReady(player1, new GlitteringLynx());
        harness.setHand(player2, List.of(new SearingWind()));
        harness.addMana(player2, ManaColor.COLORLESS, 8);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, lynx.getId());

        assertThat(lynx.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lynx);
    }

    @Test
    @DisplayName("Prevents combat damage that would be dealt to it")
    void preventsCombatDamageToIt() {
        Permanent lynx = addCreatureReady(player1, new GlitteringLynx());
        Permanent snapper = addCreatureReady(player2, new VintaraSnapper());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(lynx.getMarkedDamage()).isZero();
        assertThat(snapper.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lynx);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(snapper);
    }

    @Test
    @DisplayName("Any player may pay to turn off the prevention ability")
    void anyPlayerMayTurnOffPrevention() {
        Permanent lynx = addCreatureReady(player1, new GlitteringLynx());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SearingWind()));
        harness.addMana(player2, ManaColor.COLORLESS, 8);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, lynx.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lynx);
    }

    @Test
    @DisplayName("The controller may also pay to turn off the prevention ability")
    void controllerMayTurnOffPrevention() {
        Permanent lynx = addCreatureReady(player1, new GlitteringLynx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SearingWind()));
        harness.addMana(player2, ManaColor.COLORLESS, 8);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, lynx.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lynx);
    }

    @Test
    @DisplayName("The prevention ability returns after end-of-turn cleanup")
    void preventionReturnsAfterEndOfTurn() {
        Permanent lynx = addCreatureReady(player1, new GlitteringLynx());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, lynx.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SearingWind()));
        harness.addMana(player2, ManaColor.COLORLESS, 8);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, lynx.getId());

        assertThat(lynx.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lynx);
    }

    @Test
    @DisplayName("Damage in response to the activation is still prevented")
    void preventionRemainsUntilActivationResolves() {
        Permanent lynx = addCreatureReady(player1, new GlitteringLynx());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);

        harness.setHand(player2, List.of(new SearingWind(), new SearingWind()));
        harness.addMana(player2, ManaColor.COLORLESS, 16);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, lynx.getId());

        assertThat(lynx.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lynx);

        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, lynx.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lynx);
    }

    @Test
    @DisplayName("Turning off prevention affects only the activated Lynx")
    void suppressionAffectsOnlyItsSource() {
        Permanent suppressedLynx = addCreatureReady(player1, new GlitteringLynx());
        Permanent protectedLynx = addCreatureReady(player1, new GlitteringLynx());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SearingWind(), new SearingWind()));
        harness.addMana(player2, ManaColor.COLORLESS, 16);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, protectedLynx.getId());

        assertThat(protectedLynx.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedLynx);

        harness.castAndResolveInstant(player2, 0, suppressedLynx.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(suppressedLynx);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedLynx);
    }

    @Test
    @DisplayName("The activated ability remains available after prevention is disabled")
    void canActivateAgainAfterSuppression() {
        Permanent lynx = addCreatureReady(player1, new GlitteringLynx());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SearingWind()));
        harness.addMana(player2, ManaColor.COLORLESS, 8);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, lynx.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lynx);
    }
}
