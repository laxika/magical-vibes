package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSharedCardTypeWithImprintEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SemblanceAnvilTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB imprint MayEffect and static cost reduction effect")
    void hasCorrectStructure() {
        SemblanceAnvil card = new SemblanceAnvil();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(may.wrapped()).isInstanceOf(ExileFromHandToImprintEffect.class);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ReduceOwnCastCostForSharedCardTypeWithImprintEffect.class);
        ReduceOwnCastCostForSharedCardTypeWithImprintEffect effect =
                (ReduceOwnCastCostForSharedCardTypeWithImprintEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.amount()).isEqualTo(2);
    }

    // ===== ETB imprint =====

    @Test
    @DisplayName("ETB triggers may ability to exile nonland card from hand")
    void etbTriggersImprintChoice() {
        harness.setHand(player1, List.of(new SemblanceAnvil(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Anvil → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting imprint exiles nonland card from hand and imprints it")
    void acceptImprintExilesAndImprints() {
        harness.setHand(player1, List.of(new SemblanceAnvil(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Anvil → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();

        // Should be awaiting card choice from hand
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.IMPRINT_FROM_HAND_CHOICE);

        // Choose the creature (index 0 in remaining hand)
        harness.handleCardChosen(player1, 0);

        // Grizzly Bears should be exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Grizzly Bears should no longer be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Anvil should have Grizzly Bears imprinted
        Permanent anvil = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Semblance Anvil"))
                .findFirst().orElseThrow();
        assertThat(anvil.getCard().getImprintedCard()).isNotNull();
        assertThat(anvil.getCard().getImprintedCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Declining imprint leaves card in hand")
    void declineImprintLeavesCardInHand() {
        harness.setHand(player1, List.of(new SemblanceAnvil(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Anvil → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // Grizzly Bears should still be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Anvil should have nothing imprinted
        Permanent anvil = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Semblance Anvil"))
                .findFirst().orElseThrow();
        assertThat(anvil.getCard().getImprintedCard()).isNull();
    }

    @Test
    @DisplayName("Only land cards in hand skips imprint gracefully")
    void onlyLandsInHandSkips() {
        harness.setHand(player1, List.of(new SemblanceAnvil(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Anvil → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        // Accept may — inner effect resolves inline, but no nonland cards → skip
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();

        // Forest should still be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));

        // Anvil should have nothing imprinted
        Permanent anvil = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Semblance Anvil"))
                .findFirst().orElseThrow();
        assertThat(anvil.getCard().getImprintedCard()).isNull();
    }

    // ===== Cost reduction =====

    @Test
    @DisplayName("Creature spells cost {2} less when a creature is imprinted")
    void creatureSpellsCostLessWithCreatureImprinted() {
        // Set up Anvil with a creature imprinted
        SemblanceAnvil anvilCard = new SemblanceAnvil();
        GrizzlyBears imprintedBears = new GrizzlyBears();
        anvilCard.setImprintedCard(imprintedBears);
        harness.addToBattlefield(player1, anvilCard);

        // Grizzly Bears costs {1}{G}. With {2} reduction, only needs {G}.
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Artifact spells are not reduced when only a creature is imprinted")
    void artifactSpellsNotReducedWithCreatureImprinted() {
        // Set up Anvil with a creature imprinted
        SemblanceAnvil anvilCard = new SemblanceAnvil();
        GrizzlyBears imprintedBears = new GrizzlyBears();
        anvilCard.setImprintedCard(imprintedBears);
        harness.addToBattlefield(player1, anvilCard);

        // Golem's Heart costs {2}. No reduction (artifact ≠ creature).
        harness.setHand(player1, List.of(new GolemsHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // 1 mana is not enough (needs full 2)
        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Artifact spells cost {2} less when an artifact is imprinted")
    void artifactSpellsCostLessWithArtifactImprinted() {
        // Set up Anvil with an artifact imprinted
        SemblanceAnvil anvilCard = new SemblanceAnvil();
        GolemsHeart imprintedHeart = new GolemsHeart();
        anvilCard.setImprintedCard(imprintedHeart);
        harness.addToBattlefield(player1, anvilCard);

        // Golem's Heart costs {2}. With {2} reduction, it's free.
        harness.setHand(player1, List.of(new GolemsHeart()));

        harness.castArtifact(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Golem's Heart");
    }

    @Test
    @DisplayName("No cost reduction without an imprinted card")
    void noCostReductionWithoutImprint() {
        // Set up Anvil with nothing imprinted
        harness.addToBattlefield(player1, new SemblanceAnvil());

        // Grizzly Bears costs {1}{G}. No reduction (nothing imprinted).
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // 1 green is not enough (needs {1}{G} = 2 total)
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Two Semblance Anvils stack cost reduction to {4}")
    void twoAnvilsStackReduction() {
        // Set up two Anvils, both with creatures imprinted
        SemblanceAnvil anvil1 = new SemblanceAnvil();
        anvil1.setImprintedCard(new GrizzlyBears());
        harness.addToBattlefield(player1, anvil1);

        SemblanceAnvil anvil2 = new SemblanceAnvil();
        anvil2.setImprintedCard(new GrizzlyBears());
        harness.addToBattlefield(player1, anvil2);

        // Grizzly Bears costs {1}{G}. With {4} total reduction, only needs {G}.
        // (reduction of 4 on 1 generic = 0 generic, so just colored {G})
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Cost reduction does not affect opponent's spells")
    void costReductionDoesNotAffectOpponent() {
        // Set up Anvil under player1's control with a creature imprinted
        SemblanceAnvil anvilCard = new SemblanceAnvil();
        anvilCard.setImprintedCard(new GrizzlyBears());
        harness.addToBattlefield(player1, anvilCard);

        // Player 2 tries to cast a creature — should not get reduction
        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        // 1 green is not enough for player2 (no reduction from player1's Anvil)
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
