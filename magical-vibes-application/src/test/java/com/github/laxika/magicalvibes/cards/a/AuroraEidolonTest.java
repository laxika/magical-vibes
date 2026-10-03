package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.s.SealOfFire;
import com.github.laxika.magicalvibes.cards.t.TransguildCourier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuroraEidolon.class, MistralCharger.class, SealOfFire.class, TransguildCourier.class})
class AuroraEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Aurora Eidolon prevents the next 3 damage to a player")
    void sacrificeAbilityPreventsDamageToPlayer() {
        harness.addToBattlefield(player1, new AuroraEidolon());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aurora Eidolon");
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(3);

        harness.addToBattlefield(player1, new SealOfFire());
        harness.addToBattlefield(player1, new SealOfFire());
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerDamagePreventionShields).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("The prevention ability can target a creature")
    void preventionAbilityTargetsCreature() {
        harness.addToBattlefield(player1, new AuroraEidolon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TransguildCourier());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new SealOfFire());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a multicolored spell may return Aurora Eidolon from the graveyard")
    void multicoloredSpellReturnsEidolonToHand() {
        AuroraEidolon eidolon = new AuroraEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new TransguildCourier(), "{4}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Aurora Eidolon");
        harness.assertNotInGraveyard(player1, "Aurora Eidolon");
    }

    @Test
    @DisplayName("Declining the multicolored spell trigger keeps Aurora Eidolon in the graveyard")
    void decliningReturnKeepsEidolonInGraveyard() {
        AuroraEidolon eidolon = new AuroraEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new TransguildCourier(), "{4}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Aurora Eidolon");
    }

    @Test
    @DisplayName("A monocolored spell does not trigger Aurora Eidolon's graveyard ability")
    void monocoloredSpellDoesNotTriggerReturn() {
        AuroraEidolon eidolon = new AuroraEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Aurora Eidolon");
    }

    @Test
    @DisplayName("An opponent's multicolored spell does not trigger the return")
    void opponentsMulticoloredSpellDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new AuroraEidolon()));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new TransguildCourier(), "{4}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Aurora Eidolon");
        harness.assertNotInHand(player1, "Aurora Eidolon");
    }

    @Test
    @DisplayName("The return ability does not trigger while Aurora Eidolon is on the battlefield")
    void battlefieldEidolonDoesNotTriggerReturn() {
        harness.addToBattlefield(player1, new AuroraEidolon());
        harness.castFromHand(player1, new TransguildCourier(), "{4}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aurora Eidolon");
        harness.assertNotInHand(player1, "Aurora Eidolon");
    }

    @Test
    @DisplayName("The return trigger cannot return an Eidolon that has left the graveyard")
    void returnDoesNothingIfSourceLeftGraveyard() {
        harness.setGraveyard(player1, List.of(new AuroraEidolon()));
        harness.castFromHand(player1, new TransguildCourier(), "{4}");
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotInHand(player1, "Aurora Eidolon");
        harness.assertNotInGraveyard(player1, "Aurora Eidolon");
    }
}
