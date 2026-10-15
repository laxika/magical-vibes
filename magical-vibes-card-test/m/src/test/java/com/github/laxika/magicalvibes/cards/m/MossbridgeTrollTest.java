package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrappleWithDeath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RevokePrivileges;
import com.github.laxika.magicalvibes.cards.t.TorporDust;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MossbridgeTroll.class, AvatarOfMight.class, GrizzlyBears.class, GrappleWithDeath.class,
        RevokePrivileges.class, TorporDust.class})
class MossbridgeTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Intrinsic regeneration saves the Troll from a destroy effect without any shield")
    void intrinsicRegenSavesFromDestroy() {
        Permanent troll = addCreatureReady(player1, new MossbridgeTroll());
        harness.setHand(player2, List.of(new GrappleWithDeath()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addGrappleMana();

        harness.castAndResolveSorcery(player2, 0, 0, troll.getId());

        // Survives via intrinsic regeneration — no shield was ever set.
        harness.assertOnBattlefield(player1, "Mossbridge Troll");
        harness.assertNotInGraveyard(player1, "Mossbridge Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Intrinsic regeneration works every time — not consumed like a one-shot shield")
    void intrinsicRegenIsRepeatable() {
        Permanent troll = addCreatureReady(player1, new MossbridgeTroll());
        harness.setHand(player2, List.of(new GrappleWithDeath(), new GrappleWithDeath()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addGrappleMana();
        harness.castAndResolveSorcery(player2, 0, 0, troll.getId());

        harness.assertOnBattlefield(player1, "Mossbridge Troll");

        // Destroy it a second time the same game — a spent shield would be gone, intrinsic regen is not.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addGrappleMana();
        harness.castAndResolveSorcery(player2, 0, 0, troll.getId());

        harness.assertOnBattlefield(player1, "Mossbridge Troll");
        harness.assertNotInGraveyard(player1, "Mossbridge Troll");
    }

    @Test
    @DisplayName("Intrinsic regeneration cannot save the Troll when it can't be regenerated this turn")
    void cantRegenerateThisTurnBeatsIntrinsicRegen() {
        Permanent troll = addCreatureReady(player1, new MossbridgeTroll());
        troll.setCantRegenerateThisTurn(true);

        harness.setHand(player2, List.of(new GrappleWithDeath()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addGrappleMana();

        harness.castAndResolveSorcery(player2, 0, 0, troll.getId());

        harness.assertNotOnBattlefield(player1, "Mossbridge Troll");
        harness.assertInGraveyard(player1, "Mossbridge Troll");
    }

    @Test
    @DisplayName("Tapping 10 power of other creatures gives the Troll +20/+20")
    void pumpAbilityGrantsBoost() {
        Permanent troll = addCreatureReady(player1, new MossbridgeTroll());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight()); // 8 power
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());    // 2 power -> total 10

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(25);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(25);
        // The crew creatures are tapped; the Troll itself is not.
        assertThat(avatar.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(troll.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The +20/+20 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent troll = addCreatureReady(player1, new MossbridgeTroll());
        addCreatureReady(player1, new AvatarOfMight());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(25);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot activate the pump ability without 10 total power to tap")
    void cannotActivateWithoutEnoughPower() {
        addCreatureReady(player1, new MossbridgeTroll());
        addCreatureReady(player1, new GrizzlyBears()); // only 2 power available

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void creaturesThatCannotCrewCanStillPayThePumpCost() {
        Permanent troll = addCreatureReady(player1, new MossbridgeTroll());
        Permanent first = addCreatureReady(player1, new MossbridgeTroll());
        Permanent second = addCreatureReady(player1, new MossbridgeTroll());
        harness.setHand(player1, List.of(new RevokePrivileges()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, first.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(25);
    }

    @Test
    void negativePowerMustReduceTheTotalPowerPaid() {
        addCreatureReady(player1, new MossbridgeTroll());
        Permanent negative = addCreatureReady(player1, new MossbridgeTroll());
        Permanent first = addCreatureReady(player1, new MossbridgeTroll());
        Permanent second = addCreatureReady(player1, new MossbridgeTroll());
        addCreatureReady(player1, new MossbridgeTroll());
        harness.setHand(player1, List.of(new TorporDust(), new TorporDust()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, negative.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, negative.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, negative)).isEqualTo(-1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, negative.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());

        // The selected creatures have only nine total power; another creature is required.
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
    }

    @Test
    void tappedTrollCanUseSummoningSickCreaturesToPayTheCost() {
        Permanent troll = addCreatureReady(player1, new MossbridgeTroll());
        troll.tap();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MossbridgeTroll());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MossbridgeTroll());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(25);
    }

    @Test
    void lethalCombatDamageRegeneratesBothTrollsAndRemovesThemFromCombat() {
        Permanent attacker = addCreatureReady(player1, new MossbridgeTroll());
        Permanent blocker = addCreatureReady(player2, new MossbridgeTroll());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Mossbridge Troll");
        harness.assertOnBattlefield(player2, "Mossbridge Troll");
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(blocker.isTapped()).isTrue();
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(blocker.isBlocking()).isFalse();
    }

    // Grapple with Death costs {1}{B}{G}.
    private void addGrappleMana() {
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }
}
