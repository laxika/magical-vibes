package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrappleWithDeath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MossbridgeTrollTest extends BaseCardTest {

    // ===== Intrinsic regeneration: "If this creature would be destroyed, regenerate it." =====

    @Test
    @DisplayName("Intrinsic regeneration saves the Troll from a destroy effect without any shield")
    void intrinsicRegenSavesFromDestroy() {
        Permanent troll = addCreatureReady(player1, new MossbridgeTroll());
        harness.setHand(player2, List.of(new GrappleWithDeath()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addGrappleMana();

        harness.castSorcery(player2, 0, 0, troll.getId());
        harness.passBothPriorities();

        // Survives via intrinsic regeneration — no shield was ever set.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mossbridge Troll"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Mossbridge Troll"));
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
        harness.castSorcery(player2, 0, 0, troll.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mossbridge Troll"));

        // Destroy it a second time the same game — a spent shield would be gone, intrinsic regen is not.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addGrappleMana();
        harness.castSorcery(player2, 0, 0, troll.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mossbridge Troll"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Mossbridge Troll"));
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

        harness.castSorcery(player2, 0, 0, troll.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mossbridge Troll"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mossbridge Troll"));
    }

    // ===== Pump ability: tap creatures with total power 10+ for +20/+20 =====

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

    // Grapple with Death costs {1}{B}{G}.
    private void addGrappleMana() {
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }
}
