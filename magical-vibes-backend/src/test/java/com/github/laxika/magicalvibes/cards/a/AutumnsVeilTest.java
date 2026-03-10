package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Discombobulate;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.e.Enslave;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantControllerCreaturesCantBeTargetedByColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerSpellsCantBeCounteredByColorsEffect;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutumnsVeilTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Autumn's Veil has correct effects")
    void hasCorrectEffects() {
        AutumnsVeil card = new AutumnsVeil();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(GrantControllerSpellsCantBeCounteredByColorsEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(GrantControllerCreaturesCantBeTargetedByColorsEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Autumn's Veil puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new AutumnsVeil()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Autumn's Veil");
    }

    // ===== Counter protection =====

    @Nested
    @DisplayName("Spells can't be countered by blue or black spells")
    class CounterProtection {

        @Test
        @DisplayName("Blue counter spell fails to counter after Autumn's Veil resolves")
        void blueCounterSpellFails() {
            // Cast and resolve Autumn's Veil first
            harness.setHand(player1, List.of(new AutumnsVeil()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0);

            // Reset game state for new spell cast
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Now cast a creature spell
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new Cancel()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, bears.getId());
            // Cancel resolves first (top of stack) but fails to counter
            harness.passBothPriorities();
            // Bears is still on the stack — resolve it
            harness.passBothPriorities();

            // Creature should enter the battlefield since Cancel couldn't counter it
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Counter protection sets game data flags correctly")
        void setsGameDataFlags() {
            harness.setHand(player1, List.of(new AutumnsVeil()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0);

            assertThat(gd.playerSpellsCantBeCounteredByColorsThisTurn).containsKey(player1.getId());
            assertThat(gd.playerSpellsCantBeCounteredByColorsThisTurn.get(player1.getId()))
                    .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
        }

        @Test
        @DisplayName("Autumn's Veil itself can be countered by blue spell before it resolves")
        void autumnsVeilItselfCanBeCountered() {
            AutumnsVeil veil = new AutumnsVeil();
            harness.setHand(player1, List.of(veil));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.setHand(player2, List.of(new Cancel()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castInstant(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, veil.getId());
            // Cancel resolves first (top of stack), countering Autumn's Veil
            harness.passBothPriorities();

            // Protection flags should NOT be set since Autumn's Veil was countered
            assertThat(gd.playerSpellsCantBeCounteredByColorsThisTurn).doesNotContainKey(player1.getId());
            assertThat(gd.playerCreaturesCantBeTargetedByColorsThisTurn).doesNotContainKey(player1.getId());

            // Autumn's Veil should be in graveyard
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Autumn's Veil"));
        }

        @Test
        @DisplayName("Counter spell with additional effects — counter fails but other effects still resolve")
        void counterWithExtraEffectsPartiallyWorks() {
            // Cast and resolve Autumn's Veil first
            harness.setHand(player1, List.of(new AutumnsVeil()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0);

            // Reset game state for new spell cast
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Cast a creature spell
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new Discombobulate()));
            harness.addMana(player2, ManaColor.BLUE, 4);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, bears.getId());
            // Discombobulate resolves: counter part fails, library reorder still works
            harness.passBothPriorities();

            // Library reorder should still trigger (counter failed but other effects work)
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
            assertThat(gd.interaction.libraryView().reorderPlayerId()).isEqualTo(player2.getId());

            // Complete the library reorder so game can continue
            gs.handleLibraryCardsReordered(gd, player2, List.of(0, 1, 2, 3));

            // Bears is still on the stack — resolve it
            harness.passBothPriorities();

            // Creature should enter the battlefield (counter failed)
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Red counter spell can still counter (only blue/black blocked)")
        void redCounterSpellStillWorks() {
            harness.setHand(player1, List.of(new AutumnsVeil()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0);

            // Verify protection is set — but only protects from blue/black
            Set<CardColor> protectedColors = gd.playerSpellsCantBeCounteredByColorsThisTurn.get(player1.getId());
            assertThat(protectedColors).doesNotContain(CardColor.RED);
        }
    }

    // ===== Creature targeting protection =====

    @Nested
    @DisplayName("Creatures can't be targeted by blue or black spells")
    class CreatureTargetingProtection {

        @Test
        @DisplayName("Black spell can't target controller's creature after Autumn's Veil")
        void blackSpellCantTargetCreature() {
            // Put a creature on the battlefield
            Permanent bears = addCreature(player1);

            // Cast and resolve Autumn's Veil
            harness.setHand(player1, List.of(new AutumnsVeil()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0);

            // Reset game state for opponent's spell cast
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Opponent tries to Doom Blade the creature (black spell)
            harness.setHand(player2, List.of(new DoomBlade()));
            harness.addMana(player2, ManaColor.BLACK, 2);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);

            // Creature should still be alive
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Red spell can still target controller's creature after Autumn's Veil")
        void redSpellCanStillTargetCreature() {
            // Put a creature on the battlefield
            Permanent bears = addCreature(player1);

            // Cast and resolve Autumn's Veil
            harness.setHand(player1, List.of(new AutumnsVeil()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0);

            // Reset game state for opponent's spell cast
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Opponent tries to Shock the creature (red spell) — should work
            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            harness.castInstant(player2, 0, bears.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
        }

        @Test
        @DisplayName("Opponent's creatures are not protected by controller's Autumn's Veil")
        void opponentCreaturesNotProtected() {
            // Put a creature on opponent's battlefield
            Permanent oppBears = addCreature(player2);

            // Player 1 casts and resolves Autumn's Veil
            harness.setHand(player1, List.of(new AutumnsVeil()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0);

            // Reset game state for player1's spell cast
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Player 1 tries to Doom Blade opponent's creature — should work
            harness.setHand(player1, List.of(new DoomBlade()));
            harness.addMana(player1, ManaColor.BLACK, 2);

            harness.castInstant(player1, 0, oppBears.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Doom Blade");
        }

        @Test
        @DisplayName("Black spell already on stack fizzles after Autumn's Veil resolves")
        void blackSpellOnStackFizzlesAfterVeil() {
            // Put a creature on the battlefield
            Permanent bears = addCreature(player1);

            // Opponent casts Doom Blade targeting the creature
            harness.setHand(player2, List.of(new DoomBlade()));
            harness.addMana(player2, ManaColor.BLACK, 2);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.castInstant(player2, 0, bears.getId());

            // Player 1 casts Autumn's Veil in response
            harness.setHand(player1, List.of(new AutumnsVeil()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.passPriority(player2);
            harness.castInstant(player1, 0);

            // Autumn's Veil resolves first (top of stack)
            harness.passBothPriorities();
            // Doom Blade tries to resolve but creature is now protected — fizzles
            harness.passBothPriorities();

            // Creature should still be alive (Doom Blade fizzled)
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Black aura can't enchant a protected creature")
        void blackAuraCantEnchantProtectedCreature() {
            // Put a creature on the battlefield
            Permanent bears = addCreature(player1);

            // Cast and resolve Autumn's Veil
            harness.setHand(player1, List.of(new AutumnsVeil()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0);

            // Reset game state for opponent's spell cast
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Opponent tries to cast Enslave (black aura) targeting the creature
            harness.setHand(player2, List.of(new Enslave()));
            harness.addMana(player2, ManaColor.BLACK, 6);

            assertThatThrownBy(() -> harness.castEnchantment(player2, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);

            // Creature should still be alive and unenchanted
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Creature entering battlefield after Autumn's Veil is still protected")
        void creatureEnteringLaterIsStillProtected() {
            // Cast and resolve Autumn's Veil first (no creatures on battlefield yet)
            harness.setHand(player1, List.of(new AutumnsVeil()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0);

            // Now add a creature AFTER Autumn's Veil resolved
            Permanent bears = addCreature(player1);

            // Reset game state for opponent's spell cast
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Opponent tries to Doom Blade the creature — should fail
            harness.setHand(player2, List.of(new DoomBlade()));
            harness.addMana(player2, ManaColor.BLACK, 2);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);

            // Creature should still be alive
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Activated ability of blue creature can still target protected creature")
        void activatedAbilityCanStillTarget() {
            // Put a creature on player1's battlefield
            Permanent bears = addCreature(player1);

            // Cast and resolve Autumn's Veil for player1
            harness.setHand(player1, List.of(new AutumnsVeil()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0);

            // Put AlluringSiren (blue creature) on player2's battlefield
            AlluringSiren siren = new AlluringSiren();
            Permanent sirenPerm = new Permanent(siren);
            sirenPerm.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(sirenPerm);

            // Reset game state for ability activation
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Activate AlluringSiren's ability targeting player1's creature
            // This should work — abilities are not spells
            harness.activateAbility(player2, 0, null, bears.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        }

        @Test
        @DisplayName("Creature targeting protection sets game data flags correctly")
        void setsGameDataFlags() {
            harness.setHand(player1, List.of(new AutumnsVeil()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0);

            assertThat(gd.playerCreaturesCantBeTargetedByColorsThisTurn).containsKey(player1.getId());
            assertThat(gd.playerCreaturesCantBeTargetedByColorsThisTurn.get(player1.getId()))
                    .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
        }
    }

    // ===== End of turn cleanup =====

    @Nested
    @DisplayName("Effects expire at end of turn")
    class EndOfTurnCleanup {

        @Test
        @DisplayName("Protection flags are cleared at end of turn")
        void protectionClearedAtEndOfTurn() {
            Set<CardColor> colors = ConcurrentHashMap.newKeySet();
            colors.addAll(Set.of(CardColor.BLUE, CardColor.BLACK));
            gd.playerSpellsCantBeCounteredByColorsThisTurn.put(player1.getId(), colors);

            Set<CardColor> colors2 = ConcurrentHashMap.newKeySet();
            colors2.addAll(Set.of(CardColor.BLUE, CardColor.BLACK));
            gd.playerCreaturesCantBeTargetedByColorsThisTurn.put(player1.getId(), colors2);

            TurnCleanupService svc = new TurnCleanupService(null);
            svc.resetEndOfTurnModifiers(gd);

            assertThat(gd.playerSpellsCantBeCounteredByColorsThisTurn).isEmpty();
            assertThat(gd.playerCreaturesCantBeTargetedByColorsThisTurn).isEmpty();
        }
    }

    // ===== Helper methods =====

    private Permanent addCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
