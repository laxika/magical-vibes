package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpineOfIshSahTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ETB destroy target permanent effect")
    void hasEtbDestroyEffect() {
        SpineOfIshSah card = new SpineOfIshSah();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Has death trigger to return self from graveyard to hand")
    void hasDeathReturnToHandEffect() {
        SpineOfIshSah card = new SpineOfIshSah();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== ETB: casting and resolving =====

    @Test
    @DisplayName("Casting Spine of Ish Sah puts it on the stack with target")
    void castingPutsOnStackWithTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpineOfIshSah()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Spine of Ish Sah enters battlefield and triggers ETB")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpineOfIshSah()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0, targetId);

        // Resolve artifact spell -> enters battlefield, ETB triggers
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spine of Ish Sah");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target permanent")
    void etbDestroysTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpineOfIshSah()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0, targetId);

        // Resolve artifact spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can target and destroy an artifact")
    void etbDestroysArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new SpineOfIshSah()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.castArtifact(player1, 0, targetId);

        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    // ===== ETB: indestructible =====

    @Test
    @DisplayName("Indestructible permanent survives ETB")
    void indestructiblePermanentSurvives() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpineOfIshSah()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0, targetId);

        // Resolve artifact spell -> ETB on stack
        harness.passBothPriorities();

        // Grant indestructible before ETB resolves
        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        // Resolve ETB
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== ETB: fizzle =====

    @Test
    @DisplayName("ETB fizzles if target is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpineOfIshSah()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0, targetId);

        // Resolve artifact spell -> ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB -> fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Death trigger: return to hand =====

    @Test
    @DisplayName("Destroying Spine of Ish Sah returns it to owner's hand")
    void deathTriggerReturnsToHand() {
        harness.addToBattlefield(player1, new SpineOfIshSah());

        // Use Shatter to destroy the Spine
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        UUID targetId = harness.getPermanentId(player1, "Spine of Ish Sah");
        harness.castInstant(player2, 0, targetId);

        // Resolve Shatter -> destroys Spine
        harness.passBothPriorities();

        // Death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve death trigger
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Spine should be in hand, NOT in graveyard
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spine of Ish Sah"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Spine of Ish Sah"));
    }

}
