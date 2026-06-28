package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardAndUntapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardUnlessAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardTransformIfCreatureDiscardedEffect;
import com.github.laxika.magicalvibes.model.effect.DrawXCardsEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRandomDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPermanentNoMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageLootEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndDrawCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleHandIntoLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsReturnSelfIfCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardOrControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardXEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDiscardsEffect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingReturnToHandOnDiscardType;
import com.github.laxika.magicalvibes.model.effect.AddManaPerAttackingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaWithInstantSorceryCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToExileEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAndExileFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessExileCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndRandomDiscardWithSharedTypeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawXCardsForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.FlipTwoCoinsEffect;
import com.github.laxika.magicalvibes.model.effect.PutAwakeningCountersOnTargetLandsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomHandCardAndPlayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactThenDealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnimationSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.CardSpecificSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibraryRevealSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.NormalEffectHandlerBeanFactory;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.TapUntapSupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardUnlessExileCardFromGraveyardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Forces discard when no matching graveyard cards")
            void forcesDiscardWhenNoMatch() {
                Card card = createCard("Rotting Fensnake");
                CardPredicate predicate = mock(CardPredicate.class);
                DiscardUnlessExileCardFromGraveyardEffect effect = new DiscardUnlessExileCardFromGraveyardEffect(predicate);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                // No matching cards in graveyard
                resolveEffect(gd, entry, effect);

                assertThat(gd.discardCausedByOpponent).isFalse();
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id));
            }

            @Test
            @DisplayName("Offers may ability when matching graveyard cards exist")
            void offersMayAbilityWhenMatchExists() {
                Card card = createCard("Rotting Fensnake");
                CardPredicate predicate = mock(CardPredicate.class);
                DiscardUnlessExileCardFromGraveyardEffect effect = new DiscardUnlessExileCardFromGraveyardEffect(predicate);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                Card graveyardCard = createCard("Zombie");
                gd.playerGraveyards.get(player1Id).add(graveyardCard);

                when(gameQueryService.matchesCardPredicate(eq(graveyardCard), eq(predicate), any())).thenReturn(true);

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingMayAbilities).isNotEmpty();
                assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player1Id);
            }
}
