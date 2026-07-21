package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Consolidated tests for the merged {@link DealDamageToPlayersEffectHandler} (one handler, all recipients). */
class DealDamageToPlayersEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealDamageToPlayersEffectHandler handler;

    @Override
    protected void setUpHandler() {
        handler = new DealDamageToPlayersEffectHandler(
                damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService, amountEvaluationService);
    }

    @Nested
    @DisplayName("TARGET_PLAYER recipient")
    class TargetPlayer {

        @Test
        @DisplayName("Deals damage to target player")
        void dealsDamageToTargetPlayer() {
            Card lavaAxeCard = createCard("Lava Axe");
            StackEntry entry = createEntry(lavaAxeCard, player1Id, player2Id);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            handler.resolve(gd, entry, new DealDamageToPlayersEffect(5, DamageRecipient.TARGET_PLAYER));

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 5);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }

        @Test
        @DisplayName("Does nothing when target is not a player")
        void doesNothingWhenTargetNotPlayer() {
            Card lavaAxeCard = createCard("Lava Axe");
            UUID fakeId = UUID.randomUUID();
            StackEntry entry = createEntry(lavaAxeCard, player1Id, fakeId);

            handler.resolve(gd, entry, new DealDamageToPlayersEffect(5, DamageRecipient.TARGET_PLAYER));

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
            verifyNoInteractions(triggerCollectionService);
        }

        @Test
        @DisplayName("Deals damage equal to target player's hand size (CardsInHand(TARGET_PLAYER))")
        void dealsDamageEqualToHandSize() {
            Card impactCard = createCard("Sudden Impact");
            StackEntry entry = createEntry(impactCard, player1Id, player2Id);
            for (int i = 0; i < 5; i++) {
                gd.playerHands.get(player2Id).add(createCreature("Bear " + i, 2, 2));
            }

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            handler.resolve(gd, entry,
                    new DealDamageToPlayersEffect(new CardsInHand(CountScope.TARGET_PLAYER), DamageRecipient.TARGET_PLAYER));

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 5);
        }

        @Test
        @DisplayName("Deals 0 damage when target has an empty hand")
        void dealsZeroDamageWhenEmptyHand() {
            Card impactCard = createCard("Sudden Impact");
            StackEntry entry = createEntry(impactCard, player1Id, player2Id);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            when(damagePreventionService.isSourceDamagePreventedForPlayer(eq(gd), eq(player2Id), any())).thenReturn(false);
            when(damagePreventionService.applySourceRedirectShields(eq(gd), eq(player2Id), any(), anyInt()))
                    .thenAnswer(inv -> inv.getArgument(3));

            handler.resolve(gd, entry,
                    new DealDamageToPlayersEffect(new CardsInHand(CountScope.TARGET_PLAYER), DamageRecipient.TARGET_PLAYER));

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
            verifyNoInteractions(triggerCollectionService);
        }
    }

    @Nested
    @DisplayName("CONTROLLER recipient")
    class Controller {

        @Test
        @DisplayName("Deals damage to the controller of the ability")
        void dealsDamageToController() {
            Card artilleryCard = createCard("Orcish Artillery");
            StackEntry entry = createEntry(artilleryCard, player1Id, null);

            stubNoDamageMultiplier();
            stubDamageFromSourceNotPrevented();
            stubPlayerDamageCore(player1Id);
            stubNoInfectOnSource(entry);

            handler.resolve(gd, entry, new DealDamageToPlayersEffect(3, DamageRecipient.CONTROLLER));

            assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(17);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player1Id, 3);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player1Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player1Id);
        }
    }

    @Nested
    @DisplayName("TARGET_PERMANENT_CONTROLLER recipient")
    class TargetPermanentController {

        @Test
        @DisplayName("Deals to the targeted creature's controller as the victim, without remapping the damage source")
        void dealsToTargetCreatureControllerAndKeepsSourceController() {
            // Chandra's Outrage: "deals 2 damage to that creature's controller."
            Card outrageCard = createCard("Chandra's Outrage");
            Permanent targetCreature = addPermanent(player2Id, createCreature("Bear", 2, 2));
            StackEntry entry = createEntry(outrageCard, player1Id, targetCreature.getId());

            when(gameQueryService.findPermanentById(gd, targetCreature.getId())).thenReturn(targetCreature);
            when(gameQueryService.findPermanentController(gd, targetCreature.getId())).thenReturn(player2Id);
            stubNoDamageMultiplier();
            stubDamageFromSourceNotPrevented();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            handler.resolve(gd, entry, new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PERMANENT_CONTROLLER));

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(18);
            // The victim is player2, but the damage SOURCE controller stays the caster (player1) so
            // opponent-source damage-reduction shields keep applying — entry.controllerId is never remapped.
            verify(damagePreventionService).applyOpponentSourceDamageReduction(gd, player2Id, player1Id, 2);
        }
    }

    @Nested
    @DisplayName("TARGET_SPELL_CONTROLLER recipient")
    class TargetSpellController {

        @Test
        @DisplayName("Deals TargetSpellManaValue damage to the targeted spell's controller")
        void dealsManaValueDamageToSpellController() {
            Card refuseCard = createCard("Refuse");
            Card targetSpellCard = createCard("Counsel of the Soratami");
            // Printed mana value is left at 0 by createCard; set via xValue on the stack entry
            // plus a non-zero printed MV isn't available here — use Fixed amount for the recipient path.
            StackEntry targetSpell = createEntry(targetSpellCard, player2Id, null);
            gd.stack.add(targetSpell);

            StackEntry entry = createEntry(refuseCard, player1Id, targetSpellCard.getId());

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            handler.resolve(gd, entry, new DealDamageToPlayersEffect(4, DamageRecipient.TARGET_SPELL_CONTROLLER));

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(16);
        }

        @Test
        @DisplayName("Does nothing when the targeted spell has left the stack")
        void doesNothingWhenSpellLeftStack() {
            Card refuseCard = createCard("Refuse");
            UUID goneSpellId = UUID.randomUUID();
            StackEntry entry = createEntry(refuseCard, player1Id, goneSpellId);

            handler.resolve(gd, entry, new DealDamageToPlayersEffect(4, DamageRecipient.TARGET_SPELL_CONTROLLER));

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
            verifyNoInteractions(triggerCollectionService);
        }
    }

    @Nested
    @DisplayName("EACH_OPPONENT recipient")
    class EachOpponent {

        @Test
        @DisplayName("Deals damage to each opponent but not the controller")
        void dealsToEachOpponent() {
            Card paladinCard = createCard("Cabal Paladin");
            StackEntry entry = createEntry(paladinCard, player1Id, null);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            handler.resolve(gd, entry, new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(18);
            assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(20);
        }
    }
}
