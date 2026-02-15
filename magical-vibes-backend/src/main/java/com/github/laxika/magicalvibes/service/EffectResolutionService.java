package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetBlockingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BounceOwnCreatureOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToFlyingAndPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllEnchantmentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MillByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PlagiarizeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.filter.ControllerOnlyTargetFilter;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EffectResolutionService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final DamageResolutionService damageResolutionService;
    private final BounceResolutionService bounceResolutionService;
    private final DestructionResolutionService destructionResolutionService;
    private final LibraryResolutionService libraryResolutionService;
    private final PreventionResolutionService preventionResolutionService;
    private final CounterResolutionService counterResolutionService;
    private final GraveyardReturnResolutionService graveyardReturnResolutionService;

    void resolveEffects(GameData gameData, StackEntry entry) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            CardEffect effect = effects.get(i);
            if (effect instanceof OpponentMayPlayCreatureEffect) {
                resolveOpponentMayPlayCreature(gameData, entry.getControllerId());
            } else if (effect instanceof GainLifeEffect gainLife) {
                resolveGainLife(gameData, entry.getControllerId(), gainLife.amount());
            } else if (effect instanceof GainLifePerGraveyardCardEffect) {
                resolveGainLifePerGraveyardCard(gameData, entry.getControllerId());
            } else if (effect instanceof DestroyAllCreaturesEffect destroy) {
                destructionResolutionService.resolveDestroyAllCreatures(gameData, destroy.cannotBeRegenerated());
            } else if (effect instanceof DestroyAllEnchantmentsEffect) {
                destructionResolutionService.resolveDestroyAllEnchantments(gameData);
            } else if (effect instanceof DestroyTargetPermanentEffect destroy) {
                destructionResolutionService.resolveDestroyTargetPermanent(gameData, entry, destroy);
            } else if (effect instanceof DealXDamageToTargetCreatureEffect) {
                damageResolutionService.resolveDealXDamageToTargetCreature(gameData, entry);
            } else if (effect instanceof DealXDamageDividedAmongTargetAttackingCreaturesEffect) {
                damageResolutionService.resolveDealXDamageDividedAmongTargetAttackingCreatures(gameData, entry);
            } else if (effect instanceof DealDamageToFlyingAndPlayersEffect) {
                damageResolutionService.resolveDealDamageToFlyingAndPlayers(gameData, entry);
            } else if (effect instanceof BoostSelfEffect boost) {
                resolveBoostSelf(gameData, entry, boost);
            } else if (effect instanceof BoostTargetCreatureEffect boost) {
                resolveBoostTargetCreature(gameData, entry, boost);
            } else if (effect instanceof BoostTargetBlockingCreatureEffect boost) {
                resolveBoostTargetCreature(gameData, entry, new BoostTargetCreatureEffect(boost.powerBoost(), boost.toughnessBoost()));
            } else if (effect instanceof BoostAllOwnCreaturesEffect boost) {
                resolveBoostAllOwnCreatures(gameData, entry, boost);
            } else if (effect instanceof GrantKeywordToTargetEffect grant) {
                resolveGrantKeywordToTarget(gameData, entry, grant);
            } else if (effect instanceof MakeTargetUnblockableEffect) {
                resolveMakeTargetUnblockable(gameData, entry);
            } else if (effect instanceof PreventDamageToTargetEffect prevent) {
                preventionResolutionService.resolvePreventDamageToTarget(gameData, entry, prevent);
            } else if (effect instanceof PreventNextDamageEffect prevent) {
                preventionResolutionService.resolvePreventNextDamage(gameData, prevent);
            } else if (effect instanceof DrawCardEffect drawCard) {
                resolveDrawCards(gameData, entry.getControllerId(), drawCard.amount());
            } else if (effect instanceof DiscardCardEffect discard) {
                resolveDiscardCards(gameData, entry.getControllerId(), discard.amount());
                if (gameData.awaitingInput == AwaitingInput.DISCARD_CHOICE) {
                    break;
                }
            } else if (effect instanceof ReturnSelfToHandEffect) {
                bounceResolutionService.resolveReturnSelfToHand(gameData, entry);
            } else if (effect instanceof DoubleTargetPlayerLifeEffect) {
                resolveDoubleTargetPlayerLife(gameData, entry);
            } else if (effect instanceof ShuffleIntoLibraryEffect) {
                libraryResolutionService.resolveShuffleIntoLibrary(gameData, entry);
            } else if (effect instanceof ShuffleGraveyardIntoLibraryEffect) {
                libraryResolutionService.resolveShuffleGraveyardIntoLibrary(gameData, entry);
            } else if (effect instanceof GainLifeEqualToTargetToughnessEffect) {
                resolveGainLifeEqualToTargetToughness(gameData, entry);
            } else if (effect instanceof PutTargetOnBottomOfLibraryEffect) {
                resolvePutTargetOnBottomOfLibrary(gameData, entry);
            } else if (effect instanceof DestroyBlockedCreatureAndSelfEffect) {
                destructionResolutionService.resolveDestroyBlockedCreatureAndSelf(gameData, entry);
            } else if (effect instanceof SacrificeAtEndOfCombatEffect) {
                resolveSacrificeAtEndOfCombat(gameData, entry);
            } else if (effect instanceof PreventAllCombatDamageEffect) {
                preventionResolutionService.resolvePreventAllCombatDamage(gameData);
            } else if (effect instanceof PreventDamageFromColorsEffect prevent) {
                preventionResolutionService.resolvePreventDamageFromColors(gameData, prevent);
            } else if (effect instanceof RedirectUnblockedCombatDamageToSelfEffect) {
                resolveRedirectUnblockedCombatDamageToSelf(gameData, entry);
            } else if (effect instanceof ReturnAuraFromGraveyardToBattlefieldEffect) {
                graveyardReturnResolutionService.resolveReturnAuraFromGraveyardToBattlefield(gameData, entry);
            } else if (effect instanceof CreateCreatureTokenEffect token) {
                resolveCreateCreatureToken(gameData, entry.getControllerId(), token);
            } else if (effect instanceof ReturnCreatureFromGraveyardToBattlefieldEffect) {
                graveyardReturnResolutionService.resolveReturnCardFromGraveyardToZone(gameData, entry, CardType.CREATURE,
                        GraveyardChoiceDestination.BATTLEFIELD,
                        "You may return a creature card from your graveyard to the battlefield.");
            } else if (effect instanceof ReturnArtifactFromGraveyardToHandEffect) {
                graveyardReturnResolutionService.resolveReturnCardFromGraveyardToZone(gameData, entry, CardType.ARTIFACT,
                        GraveyardChoiceDestination.HAND,
                        "You may return an artifact card from your graveyard to your hand.");
            } else if (effect instanceof ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect) {
                graveyardReturnResolutionService.resolveReturnArtifactOrCreatureFromAnyGraveyardToBattlefield(gameData, entry);
            } else if (effect instanceof RegenerateEffect) {
                resolveRegenerate(gameData, entry);
            } else if (effect instanceof TapCreaturesEffect tap) {
                resolveTapCreatures(gameData, entry, tap);
            } else if (effect instanceof TapTargetCreatureEffect) {
                resolveTapTargetPermanent(gameData, entry);
            } else if (effect instanceof TapTargetPermanentEffect) {
                resolveTapTargetPermanent(gameData, entry);
            } else if (effect instanceof UntapSelfEffect) {
                resolveUntapSelf(gameData, entry);
            } else if (effect instanceof PreventNextColorDamageToControllerEffect prevent) {
                preventionResolutionService.resolvePreventNextColorDamageToController(gameData, entry, prevent);
            } else if (effect instanceof PutAuraFromHandOntoSelfEffect) {
                resolvePutAuraFromHandOntoSelf(gameData, entry);
            } else if (effect instanceof MillByHandSizeEffect) {
                libraryResolutionService.resolveMillByHandSize(gameData, entry);
            } else if (effect instanceof BounceOwnCreatureOnUpkeepEffect) {
                bounceResolutionService.resolveBounceOwnCreatureOnUpkeep(gameData, entry);
                if (gameData.awaitingInput == AwaitingInput.PERMANENT_CHOICE) {
                    break;
                }
            } else if (effect instanceof MillTargetPlayerEffect mill) {
                libraryResolutionService.resolveMillTargetPlayer(gameData, entry, mill);
            } else if (effect instanceof LookAtHandEffect) {
                resolveLookAtHand(gameData, entry);
            } else if (effect instanceof ChooseCardsFromTargetHandToTopOfLibraryEffect choose) {
                resolveChooseCardsFromTargetHandToTopOfLibrary(gameData, entry, choose);
                if (gameData.awaitingInput == AwaitingInput.REVEALED_HAND_CHOICE) {
                    break;
                }
            } else if (effect instanceof RevealTopCardOfLibraryEffect) {
                libraryResolutionService.resolveRevealTopCardOfLibrary(gameData, entry);
            } else if (effect instanceof GainControlOfEnchantedTargetEffect) {
                resolveGainControlOfEnchantedTarget(gameData, entry);
            } else if (effect instanceof GainControlOfTargetAuraEffect) {
                resolveGainControlOfTargetAura(gameData, entry);
            } else if (effect instanceof ReturnTargetPermanentToHandEffect) {
                bounceResolutionService.resolveReturnTargetPermanentToHand(gameData, entry);
            } else if (effect instanceof ReturnCreaturesToOwnersHandEffect bounce) {
                bounceResolutionService.resolveReturnCreaturesToOwnersHand(gameData, entry, bounce);
            } else if (effect instanceof ReturnArtifactsTargetPlayerOwnsToHandEffect) {
                bounceResolutionService.resolveReturnArtifactsTargetPlayerOwnsToHand(gameData, entry);
            } else if (effect instanceof ChangeColorTextEffect) {
                resolveChangeColorText(gameData, entry);
                if (gameData.awaitingInput == AwaitingInput.COLOR_CHOICE) {
                    break;
                }
            } else if (effect instanceof CounterSpellEffect) {
                counterResolutionService.resolveCounterSpell(gameData, entry);
            } else if (effect instanceof CounterUnlessPaysEffect counterUnless) {
                counterResolutionService.resolveCounterUnlessPays(gameData, entry, counterUnless);
                if (!gameData.pendingMayAbilities.isEmpty()) {
                    break;
                }
            } else if (effect instanceof PlagiarizeEffect) {
                resolvePlagiarize(gameData, entry);
            } else if (effect instanceof ReorderTopCardsOfLibraryEffect reorder) {
                libraryResolutionService.resolveReorderTopCardsOfLibrary(gameData, entry, reorder);
                if (gameData.awaitingInput == AwaitingInput.LIBRARY_REORDER) {
                    break;
                }
            } else if (effect instanceof SearchLibraryForBasicLandToHandEffect) {
                libraryResolutionService.resolveSearchLibraryForBasicLandToHand(gameData, entry);
                if (gameData.awaitingInput == AwaitingInput.LIBRARY_SEARCH) {
                    break;
                }
            } else if (effect instanceof LookAtTopCardsHandTopBottomEffect lookAtTop) {
                libraryResolutionService.resolveLookAtTopCardsHandTopBottom(gameData, entry, lookAtTop);
                if (gameData.awaitingInput == AwaitingInput.HAND_TOP_BOTTOM_CHOICE) {
                    break;
                }
            } else if (effect instanceof ExileCardsFromGraveyardEffect exile) {
                graveyardReturnResolutionService.resolveExileCardsFromGraveyard(gameData, entry, exile);
            }
        }
        gameHelper.removeOrphanedAuras(gameData);
    }

    // ===== Effect resolution methods =====

    private void resolveOpponentMayPlayCreature(GameData gameData, UUID controllerId) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        List<Card> opponentHand = gameData.playerHands.get(opponentId);

        List<Integer> creatureIndices = new ArrayList<>();
        if (opponentHand != null) {
            for (int i = 0; i < opponentHand.size(); i++) {
                if (opponentHand.get(i).getType() == CardType.CREATURE) {
                    creatureIndices.add(i);
                }
            }
        }

        if (creatureIndices.isEmpty()) {
            String opponentName = gameData.playerIdToName.get(opponentId);
            String logEntry = opponentName + " has no creature cards in hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no creatures in hand for ETB effect", gameData.id, opponentName);
            return;
        }

        String prompt = "You may put a creature card from your hand onto the battlefield.";
        playerInputService.beginCardChoice(gameData, opponentId, creatureIndices, prompt);
    }

    private void resolvePutAuraFromHandOntoSelf(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        Permanent self = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(entry.getCard().getId())) {
                    self = p;
                    break;
                }
            }
        }

        if (self == null) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            log.info("Game {} - {} ETB fizzles, creature left battlefield", gameData.id, entry.getCard().getName());
            return;
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        List<Integer> auraIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).isAura()) {
                    auraIndices.add(i);
                }
            }
        }

        if (auraIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " has no Aura cards in hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no Auras in hand for {} ETB", gameData.id, playerName, entry.getCard().getName());
            return;
        }

        String prompt = "You may put an Aura card from your hand onto the battlefield attached to " + entry.getCard().getName() + ".";
        playerInputService.beginTargetedCardChoice(gameData, controllerId, auraIndices, prompt, self.getId());
    }

    private void resolveGainLife(GameData gameData, UUID controllerId, int amount) {
        Integer currentLife = gameData.playerLifeTotals.get(controllerId);
        gameData.playerLifeTotals.put(controllerId, currentLife + amount);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " gains " + amount + " life.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gains {} life", gameData.id, playerName, amount);
    }

    private void resolveGainLifePerGraveyardCard(GameData gameData, UUID controllerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        int amount = graveyard != null ? graveyard.size() : 0;
        if (amount == 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " has no cards in their graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no graveyard cards for life gain", gameData.id, playerName);
            return;
        }
        resolveGainLife(gameData, controllerId, amount);
    }

    private void resolveBoostSelf(GameData gameData, StackEntry entry, BoostSelfEffect boost) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        self.setPowerModifier(self.getPowerModifier() + boost.powerBoost());
        self.setToughnessModifier(self.getToughnessModifier() + boost.toughnessBoost());

        String logEntry = self.getCard().getName() + " gets +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{}", gameData.id, self.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
    }

    private void resolveBoostTargetCreature(GameData gameData, StackEntry entry, BoostTargetCreatureEffect boost) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setPowerModifier(target.getPowerModifier() + boost.powerBoost());
        target.setToughnessModifier(target.getToughnessModifier() + boost.toughnessBoost());

        String logEntry = target.getCard().getName() + " gets +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{}", gameData.id, target.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
    }

    private void resolveBoostAllOwnCreatures(GameData gameData, StackEntry entry, BoostAllOwnCreaturesEffect boost) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                permanent.setPowerModifier(permanent.getPowerModifier() + boost.powerBoost());
                permanent.setToughnessModifier(permanent.getToughnessModifier() + boost.toughnessBoost());
                count++;
            }
        }

        String logEntry = entry.getCard().getName() + " gives +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " to " + count + " creature(s) until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} boosts {} creatures +{}/+{}", gameData.id, entry.getCard().getName(), count, boost.powerBoost(), boost.toughnessBoost());
    }

    private void resolveGrantKeywordToTarget(GameData gameData, StackEntry entry, GrantKeywordToTargetEffect grant) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.getGrantedKeywords().add(grant.keyword());

        String keywordName = grant.keyword().name().charAt(0) + grant.keyword().name().substring(1).toLowerCase().replace('_', ' ');
        String logEntry = target.getCard().getName() + " gains " + keywordName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gains {}", gameData.id, target.getCard().getName(), grant.keyword());
    }

    private void resolveMakeTargetUnblockable(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setCantBeBlocked(true);

        String logEntry = target.getCard().getName() + " can't be blocked this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} can't be blocked this turn", gameData.id, target.getCard().getName());
    }

    private void resolveDrawCards(GameData gameData, UUID playerId, int amount) {
        for (int i = 0; i < amount; i++) {
            gameHelper.resolveDrawCard(gameData, playerId);
        }
    }

    private void resolveDiscardCards(GameData gameData, UUID playerId, int amount) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to discard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.awaitingDiscardRemainingCount = amount;
        playerInputService.beginDiscardChoice(gameData, playerId);
    }

    private void resolveDoubleTargetPlayerLife(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();

        int currentLife = gameData.playerLifeTotals.getOrDefault(targetPlayerId, 20);
        int newLife = currentLife * 2;
        gameData.playerLifeTotals.put(targetPlayerId, newLife);

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = playerName + "'s life total is doubled from " + currentLife + " to " + newLife + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {}'s life doubled from {} to {}", gameData.id, playerName, currentLife, newLife);
    }

    private void resolveGainLifeEqualToTargetToughness(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        int toughness = gameQueryService.getEffectiveToughness(gameData, target);

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(target)) {
                int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
                gameData.playerLifeTotals.put(playerId, currentLife + toughness);

                String logEntry = gameData.playerIdToName.get(playerId) + " gains " + toughness + " life.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);

                log.info("Game {} - {} gains {} life (equal to {}'s toughness)",
                        gameData.id, gameData.playerIdToName.get(playerId), toughness, target.getCard().getName());
                break;
            }
        }
    }

    private void resolvePutTargetOnBottomOfLibrary(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                gameData.playerDecks.get(playerId).add(target.getOriginalCard());

                String logEntry = target.getCard().getName() + " is put on the bottom of "
                        + gameData.playerIdToName.get(playerId) + "'s library.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);

                log.info("Game {} - {} put on bottom of {}'s library",
                        gameData.id, target.getCard().getName(), gameData.playerIdToName.get(playerId));
                break;
            }
        }

        gameHelper.removeOrphanedAuras(gameData);
    }

    private void resolveSacrificeAtEndOfCombat(GameData gameData, StackEntry entry) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null) {
            gameData.permanentsToSacrificeAtEndOfCombat.add(self.getId());
            String logEntry = entry.getCard().getName() + " will be sacrificed at end of combat.";
            gameData.gameLog.add(logEntry);
        }
    }

    private void resolveRedirectUnblockedCombatDamageToSelf(GameData gameData, StackEntry entry) {
        List<Permanent> bf = gameData.playerBattlefields.get(entry.getControllerId());
        if (bf == null) return;
        for (Permanent p : bf) {
            if (p.getCard() == entry.getCard()) {
                gameData.combatDamageRedirectTarget = p.getId();

                String logEntry = p.getCard().getName() + "'s ability resolves — unblocked combat damage will be redirected to it this turn.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - Combat damage redirect set to {}", gameData.id, p.getCard().getName());
                return;
            }
        }
    }

    private void resolveCreateCreatureToken(GameData gameData, UUID controllerId, CreateCreatureTokenEffect token) {
        Card tokenCard = new Card(token.tokenName(), CardType.CREATURE, "", token.color());
        tokenCard.setPower(token.power());
        tokenCard.setToughness(token.toughness());
        tokenCard.setSubtypes(token.subtypes());

        Permanent tokenPermanent = new Permanent(tokenCard);
        gameData.playerBattlefields.get(controllerId).add(tokenPermanent);

        String logEntry = "A " + token.power() + "/" + token.toughness() + " " + token.tokenName() + " creature token enters the battlefield.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null);
        if (gameData.awaitingInput == null) {
            gameHelper.checkLegendRule(gameData, controllerId);
        }

        log.info("Game {} - {} token created for player {}", gameData.id, token.tokenName(), controllerId);
    }

    private void resolveRegenerate(GameData gameData, StackEntry entry) {
        Permanent perm = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (perm == null) {
            return;
        }
        perm.setRegenerationShield(perm.getRegenerationShield() + 1);

        String logEntry = perm.getCard().getName() + " gains a regeneration shield.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gains a regeneration shield", gameData.id, perm.getCard().getName());
    }

    private void resolveTapCreatures(GameData gameData, StackEntry entry, TapCreaturesEffect tap) {
        boolean controllerOnly = tap.filters().stream().anyMatch(f -> f instanceof ControllerOnlyTargetFilter);

        List<UUID> playerIds = controllerOnly
                ? List.of(entry.getControllerId())
                : gameData.orderedPlayerIds;

        for (UUID playerId : playerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent p : battlefield) {
                if (!gameQueryService.isCreature(gameData, p)) continue;
                if (!gameQueryService.matchesFilters(gameData, p, tap.filters())) continue;

                p.tap();

                String logMsg = entry.getCard().getName() + " taps " + p.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
            }
        }

        log.info("Game {} - {} taps creatures matching filters", gameData.id, entry.getCard().getName());
    }

    private void resolveTapTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.tap();

        String logEntry = entry.getCard().getName() + " taps " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }

    private void resolveUntapSelf(GameData gameData, StackEntry entry) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        self.untap();

        String logEntry = entry.getCard().getName() + " untaps.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} untaps", gameData.id, entry.getCard().getName());
    }

    private void resolveLookAtHand(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(entry.getControllerId());

        if (hand.isEmpty()) {
            String logEntry = casterName + " looks at " + targetName + "'s hand. It is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
            String logEntry = casterName + " looks at " + targetName + "'s hand: " + cardNames + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        List<CardView> cardViews = hand.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(entry.getControllerId(), new RevealHandMessage(cardViews, targetName));

        log.info("Game {} - {} looks at {}'s hand", gameData.id, casterName, targetName);
    }

    private void resolveChooseCardsFromTargetHandToTopOfLibrary(GameData gameData, StackEntry entry, ChooseCardsFromTargetHandToTopOfLibraryEffect choose) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);

        if (hand.isEmpty()) {
            String logEntry = casterName + " looks at " + targetName + "'s hand. It is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} looks at {}'s empty hand", gameData.id, casterName, targetName);
            return;
        }

        // Log and reveal hand to caster
        String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
        String logEntry = casterName + " looks at " + targetName + "'s hand: " + cardNames + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        int cardsToChoose = Math.min(choose.count(), hand.size());

        // Build valid indices (all cards in hand)
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }

        gameData.awaitingRevealedHandChoiceTargetPlayerId = targetPlayerId;
        gameData.awaitingRevealedHandChoiceRemainingCount = cardsToChoose;
        gameData.awaitingRevealedHandChosenCards.clear();

        playerInputService.beginRevealedHandChoice(gameData, casterId, targetPlayerId, validIndices,
                "Choose a card to put on top of " + targetName + "'s library.");

        log.info("Game {} - {} choosing {} card(s) from {}'s hand to put on top of library",
                gameData.id, casterName, cardsToChoose, targetName);
    }

    private void resolveGainControlOfTargetAura(GameData gameData, StackEntry entry) {
        UUID casterId = entry.getControllerId();
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (aura == null) return;

        UUID currentControllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(aura)) {
                currentControllerId = pid;
                break;
            }
        }
        if (currentControllerId != null && !currentControllerId.equals(casterId)) {
            gameData.playerBattlefields.get(currentControllerId).remove(aura);
            gameData.playerBattlefields.get(casterId).add(aura);
            String casterName = gameData.playerIdToName.get(casterId);
            String logEntry = casterName + " gains control of " + aura.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} gains control of {}", gameData.id, casterName, aura.getCard().getName());
        }

        List<UUID> validCreatureIds = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (gameQueryService.isCreature(gameData, p) && !p.getId().equals(aura.getAttachedTo())) {
                    validCreatureIds.add(p.getId());
                }
            }
        }

        if (!validCreatureIds.isEmpty()) {
            gameData.permanentChoiceContext = new PermanentChoiceContext.AuraGraft(aura.getId());
            playerInputService.beginPermanentChoice(gameData, casterId, validCreatureIds,
                    "Attach " + aura.getCard().getName() + " to another permanent it can enchant.");
        } else {
            String logEntry = aura.getCard().getName() + " stays attached to its current target (no other valid permanents).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }
    }

    private void resolveGainControlOfEnchantedTarget(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        if (!gameQueryService.isEnchanted(gameData, target)) {
            String logEntry = entry.getCard().getName() + "'s ability has no effect (" + target.getCard().getName() + " is not enchanted).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameHelper.stealCreature(gameData, entry.getControllerId(), target);
        gameData.enchantmentDependentStolenCreatures.add(target.getId());
    }

    private void resolveChangeColorText(GameData gameData, StackEntry entry) {
        UUID targetPermanentId = entry.getTargetPermanentId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            return;
        }

        gameData.colorChoiceContext = new ColorChoiceContext.TextChangeFromWord(targetPermanentId);
        gameData.awaitingInput = AwaitingInput.COLOR_CHOICE;
        gameData.awaitingColorChoicePlayerId = entry.getControllerId();

        List<String> options = new ArrayList<>();
        options.addAll(GameQueryService.TEXT_CHANGE_COLOR_WORDS);
        options.addAll(GameQueryService.TEXT_CHANGE_LAND_TYPES);
        sessionManager.sendToPlayer(entry.getControllerId(), new ChooseColorMessage(options, "Choose a color word or basic land type to replace."));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose a color word or basic land type for text change", gameData.id, playerName);
    }

    private void resolvePlagiarize(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        UUID controllerId = entry.getControllerId();

        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            log.warn("Game {} - Plagiarize target player not found", gameData.id);
            return;
        }

        gameData.drawReplacementTargetToController.put(targetPlayerId, controllerId);

        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "Plagiarize resolves targeting " + targetName
                + ". Until end of turn, " + targetName + "'s draws are replaced.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Plagiarize: {}'s draws replaced by {} until end of turn",
                gameData.id, targetName, controllerName);
    }
}
