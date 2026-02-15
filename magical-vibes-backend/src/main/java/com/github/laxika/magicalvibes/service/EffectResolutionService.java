package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.BounceOwnCreatureOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetBlockingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
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
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.filter.ControllerOnlyTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ExcludeSelfTargetFilter;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseFromRevealedHandMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseHandTopBottomMessage;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
                resolveDestroyAllCreatures(gameData, destroy.cannotBeRegenerated());
            } else if (effect instanceof DestroyAllEnchantmentsEffect) {
                resolveDestroyAllEnchantments(gameData);
            } else if (effect instanceof DestroyTargetPermanentEffect destroy) {
                resolveDestroyTargetPermanent(gameData, entry, destroy);
            } else if (effect instanceof DealXDamageToTargetCreatureEffect) {
                resolveDealXDamageToTargetCreature(gameData, entry);
            } else if (effect instanceof DealXDamageDividedAmongTargetAttackingCreaturesEffect) {
                resolveDealXDamageDividedAmongTargetAttackingCreatures(gameData, entry);
            } else if (effect instanceof DealDamageToFlyingAndPlayersEffect) {
                resolveDealDamageToFlyingAndPlayers(gameData, entry);
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
                resolvePreventDamageToTarget(gameData, entry, prevent);
            } else if (effect instanceof PreventNextDamageEffect prevent) {
                resolvePreventNextDamage(gameData, prevent);
            } else if (effect instanceof DrawCardEffect drawCard) {
                resolveDrawCards(gameData, entry.getControllerId(), drawCard.amount());
            } else if (effect instanceof DiscardCardEffect discard) {
                resolveDiscardCards(gameData, entry.getControllerId(), discard.amount());
                if (gameData.awaitingInput == AwaitingInput.DISCARD_CHOICE) {
                    break;
                }
            } else if (effect instanceof ReturnSelfToHandEffect) {
                resolveReturnSelfToHand(gameData, entry);
            } else if (effect instanceof DoubleTargetPlayerLifeEffect) {
                resolveDoubleTargetPlayerLife(gameData, entry);
            } else if (effect instanceof ShuffleIntoLibraryEffect) {
                List<Card> deck = gameData.playerDecks.get(entry.getControllerId());
                deck.add(entry.getCard());
                Collections.shuffle(deck);

                String shuffleLog = entry.getCard().getName() + " is shuffled into its owner's library.";
                gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
            } else if (effect instanceof ShuffleGraveyardIntoLibraryEffect) {
                resolveShuffleGraveyardIntoLibrary(gameData, entry);
            } else if (effect instanceof GainLifeEqualToTargetToughnessEffect) {
                resolveGainLifeEqualToTargetToughness(gameData, entry);
            } else if (effect instanceof PutTargetOnBottomOfLibraryEffect) {
                resolvePutTargetOnBottomOfLibrary(gameData, entry);
            } else if (effect instanceof DestroyBlockedCreatureAndSelfEffect) {
                resolveDestroyBlockedCreatureAndSelf(gameData, entry);
            } else if (effect instanceof SacrificeAtEndOfCombatEffect) {
                resolveSacrificeAtEndOfCombat(gameData, entry);
            } else if (effect instanceof PreventAllCombatDamageEffect) {
                resolvePreventAllCombatDamage(gameData);
            } else if (effect instanceof PreventDamageFromColorsEffect prevent) {
                resolvePreventDamageFromColors(gameData, prevent);
            } else if (effect instanceof RedirectUnblockedCombatDamageToSelfEffect) {
                resolveRedirectUnblockedCombatDamageToSelf(gameData, entry);
            } else if (effect instanceof ReturnAuraFromGraveyardToBattlefieldEffect) {
                resolveReturnAuraFromGraveyardToBattlefield(gameData, entry);
            } else if (effect instanceof CreateCreatureTokenEffect token) {
                resolveCreateCreatureToken(gameData, entry.getControllerId(), token);
            } else if (effect instanceof ReturnCreatureFromGraveyardToBattlefieldEffect) {
                resolveReturnCardFromGraveyardToZone(gameData, entry, CardType.CREATURE,
                        GraveyardChoiceDestination.BATTLEFIELD,
                        "You may return a creature card from your graveyard to the battlefield.");
            } else if (effect instanceof ReturnArtifactFromGraveyardToHandEffect) {
                resolveReturnCardFromGraveyardToZone(gameData, entry, CardType.ARTIFACT,
                        GraveyardChoiceDestination.HAND,
                        "You may return an artifact card from your graveyard to your hand.");
            } else if (effect instanceof ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect) {
                resolveReturnArtifactOrCreatureFromAnyGraveyardToBattlefield(gameData, entry);
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
                resolvePreventNextColorDamageToController(gameData, entry, prevent);
            } else if (effect instanceof PutAuraFromHandOntoSelfEffect) {
                resolvePutAuraFromHandOntoSelf(gameData, entry);
            } else if (effect instanceof MillByHandSizeEffect) {
                resolveMillByHandSize(gameData, entry);
            } else if (effect instanceof BounceOwnCreatureOnUpkeepEffect) {
                resolveBounceOwnCreatureOnUpkeep(gameData, entry);
                if (gameData.awaitingInput == AwaitingInput.PERMANENT_CHOICE) {
                    break;
                }
            } else if (effect instanceof MillTargetPlayerEffect mill) {
                resolveMillTargetPlayer(gameData, entry, mill);
            } else if (effect instanceof LookAtHandEffect) {
                resolveLookAtHand(gameData, entry);
            } else if (effect instanceof ChooseCardsFromTargetHandToTopOfLibraryEffect choose) {
                resolveChooseCardsFromTargetHandToTopOfLibrary(gameData, entry, choose);
                if (gameData.awaitingInput == AwaitingInput.REVEALED_HAND_CHOICE) {
                    break;
                }
            } else if (effect instanceof RevealTopCardOfLibraryEffect) {
                resolveRevealTopCardOfLibrary(gameData, entry);
            } else if (effect instanceof GainControlOfEnchantedTargetEffect) {
                resolveGainControlOfEnchantedTarget(gameData, entry);
            } else if (effect instanceof GainControlOfTargetAuraEffect) {
                resolveGainControlOfTargetAura(gameData, entry);
            } else if (effect instanceof ReturnTargetPermanentToHandEffect) {
                resolveReturnTargetPermanentToHand(gameData, entry);
            } else if (effect instanceof ReturnCreaturesToOwnersHandEffect bounce) {
                resolveReturnCreaturesToOwnersHand(gameData, entry, bounce);
            } else if (effect instanceof ReturnArtifactsTargetPlayerOwnsToHandEffect) {
                resolveReturnArtifactsTargetPlayerOwnsToHand(gameData, entry);
            } else if (effect instanceof ChangeColorTextEffect) {
                resolveChangeColorText(gameData, entry);
                if (gameData.awaitingInput == AwaitingInput.COLOR_CHOICE) {
                    break;
                }
            } else if (effect instanceof CounterSpellEffect) {
                resolveCounterSpell(gameData, entry);
            } else if (effect instanceof CounterUnlessPaysEffect counterUnless) {
                resolveCounterUnlessPays(gameData, entry, counterUnless);
                if (!gameData.pendingMayAbilities.isEmpty()) {
                    break;
                }
            } else if (effect instanceof PlagiarizeEffect) {
                resolvePlagiarize(gameData, entry);
            } else if (effect instanceof ReorderTopCardsOfLibraryEffect reorder) {
                resolveReorderTopCardsOfLibrary(gameData, entry, reorder);
                if (gameData.awaitingInput == AwaitingInput.LIBRARY_REORDER) {
                    break;
                }
            } else if (effect instanceof SearchLibraryForBasicLandToHandEffect) {
                resolveSearchLibraryForBasicLandToHand(gameData, entry);
                if (gameData.awaitingInput == AwaitingInput.LIBRARY_SEARCH) {
                    break;
                }
            } else if (effect instanceof LookAtTopCardsHandTopBottomEffect lookAtTop) {
                resolveLookAtTopCardsHandTopBottom(gameData, entry, lookAtTop);
                if (gameData.awaitingInput == AwaitingInput.HAND_TOP_BOTTOM_CHOICE) {
                    break;
                }
            } else if (effect instanceof ExileCardsFromGraveyardEffect exile) {
                resolveExileCardsFromGraveyard(gameData, entry, exile);
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

    private void resolveDealXDamageToTargetCreature(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())
                || gameQueryService.hasProtectionFrom(gameData, target, entry.getCard().getColor())) {
            String logEntry = entry.getCard().getName() + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        int damage = gameHelper.applyCreaturePreventionShield(gameData, target, entry.getXValue());
        String logEntry = entry.getCard().getName() + " deals " + damage + " damage to " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} deals {} damage to {}", gameData.id, entry.getCard().getName(), damage, target.getCard().getName());

        if (damage >= gameQueryService.getEffectiveToughness(gameData, target)) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                String indestructibleLog = target.getCard().getName() + " is indestructible and survives.";
                gameBroadcastService.logAndBroadcast(gameData, indestructibleLog);
            } else if (gameHelper.tryRegenerate(gameData, target)) {

            } else {
                gameHelper.removePermanentToGraveyard(gameData, target);
                String destroyLog = target.getCard().getName() + " is destroyed.";
                gameBroadcastService.logAndBroadcast(gameData, destroyLog);
                log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
                gameHelper.removeOrphanedAuras(gameData);
            }
        }
    }

    private void resolveDealXDamageDividedAmongTargetAttackingCreatures(GameData gameData, StackEntry entry) {
        Map<UUID, Integer> assignments = entry.getDamageAssignments();
        if (assignments == null || assignments.isEmpty()) {
            return;
        }

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logEntry = entry.getCard().getName() + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        List<Permanent> destroyed = new ArrayList<>();

        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
            if (target == null) {
                continue;
            }
            if (gameQueryService.hasProtectionFrom(gameData, target, entry.getCard().getColor())) {
                continue;
            }

            int damage = gameHelper.applyCreaturePreventionShield(gameData, target, assignment.getValue());
            String logEntry = entry.getCard().getName() + " deals " + damage + " damage to " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} deals {} damage to {}", gameData.id, entry.getCard().getName(), damage, target.getCard().getName());

            if (damage >= target.getEffectiveToughness()) {
                if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                    String indestructibleLog = target.getCard().getName() + " is indestructible and survives.";
                    gameBroadcastService.logAndBroadcast(gameData, indestructibleLog);
                } else if (!gameHelper.tryRegenerate(gameData, target)) {
                    destroyed.add(target);
                }
            }
        }

        for (Permanent target : destroyed) {
            gameHelper.removePermanentToGraveyard(gameData, target);
            String destroyLog = target.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, destroyLog);
            log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
        }

        if (!destroyed.isEmpty()) {
            gameHelper.removeOrphanedAuras(gameData);
        }
    }

    private void resolveDestroyAllCreatures(GameData gameData, boolean cannotBeRegenerated) {
        List<Permanent> toDestroy = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent perm : gameData.playerBattlefields.get(playerId)) {
                if (gameQueryService.isCreature(gameData, perm)) {
                    toDestroy.add(perm);
                }
            }
        }

        // Snapshot indestructible status before any removals (MTG rules: "destroy all" is simultaneous)
        Set<Permanent> indestructible = new HashSet<>();
        for (Permanent perm : toDestroy) {
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                indestructible.add(perm);
            }
        }

        for (Permanent perm : toDestroy) {
            if (indestructible.contains(perm)) {
                String logEntry = perm.getCard().getName() + " is indestructible.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                continue;
            }
            if (!cannotBeRegenerated && gameHelper.tryRegenerate(gameData, perm)) {
                continue;
            }
            gameHelper.removePermanentToGraveyard(gameData, perm);
            String logEntry = perm.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is destroyed", gameData.id, perm.getCard().getName());
        }
    }

    private void resolveDestroyAllEnchantments(GameData gameData) {
        List<Permanent> toDestroy = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent perm : gameData.playerBattlefields.get(playerId)) {
                if (perm.getCard().getType() == CardType.ENCHANTMENT) {
                    toDestroy.add(perm);
                }
            }
        }

        // Snapshot indestructible status before any removals (MTG rules: "destroy all" is simultaneous)
        Set<Permanent> indestructible = new HashSet<>();
        for (Permanent perm : toDestroy) {
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                indestructible.add(perm);
            }
        }

        for (Permanent perm : toDestroy) {
            if (indestructible.contains(perm)) {
                String logEntry = perm.getCard().getName() + " is indestructible.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                continue;
            }
            gameHelper.removePermanentToGraveyard(gameData, perm);
            String logEntry = perm.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is destroyed", gameData.id, perm.getCard().getName());
        }
    }

    private void resolveDestroyTargetPermanent(GameData gameData, StackEntry entry, DestroyTargetPermanentEffect destroy) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (!destroy.targetTypes().contains(target.getCard().getType())) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (invalid target type).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            log.info("Game {} - {}'s ability fizzles, target type mismatch", gameData.id, entry.getCard().getName());
            return;
        }

        if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
            String logEntry = target.getCard().getName() + " is indestructible.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is indestructible, destroy prevented", gameData.id, target.getCard().getName());
            return;
        }

        if (gameQueryService.isCreature(gameData, target) && gameHelper.tryRegenerate(gameData, target)) {
            return;
        }

        gameHelper.removePermanentToGraveyard(gameData, target);
        String logEntry = target.getCard().getName() + " is destroyed.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is destroyed by {}'s ability",
                gameData.id, target.getCard().getName(), entry.getCard().getName());

        gameHelper.removeOrphanedAuras(gameData);
    }

    private void resolveDestroyBlockedCreatureAndSelf(GameData gameData, StackEntry entry) {
        Permanent attacker = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (attacker != null) {
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.INDESTRUCTIBLE)) {
                String logEntry = attacker.getCard().getName() + " is indestructible.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else if (!gameHelper.tryRegenerate(gameData, attacker)) {
                gameHelper.removePermanentToGraveyard(gameData, attacker);
                String logEntry = attacker.getCard().getName() + " is destroyed by " + entry.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} destroyed by {}'s block trigger", gameData.id, attacker.getCard().getName(), entry.getCard().getName());
            }
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null) {
            if (gameQueryService.hasKeyword(gameData, self, Keyword.INDESTRUCTIBLE)) {
                String logEntry = entry.getCard().getName() + " is indestructible.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else if (!gameHelper.tryRegenerate(gameData, self)) {
                gameHelper.removePermanentToGraveyard(gameData, self);
                String logEntry = entry.getCard().getName() + " is destroyed.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} destroyed (self-destruct from block trigger)", gameData.id, entry.getCard().getName());
            }
        }
    }

    private void resolveSacrificeAtEndOfCombat(GameData gameData, StackEntry entry) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null) {
            gameData.permanentsToSacrificeAtEndOfCombat.add(self.getId());
            String logEntry = entry.getCard().getName() + " will be sacrificed at end of combat.";
            gameData.gameLog.add(logEntry);
        }
    }

    private void resolvePreventDamageToTarget(GameData gameData, StackEntry entry, PreventDamageToTargetEffect prevent) {
        UUID targetId = entry.getTargetPermanentId();

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            target.setDamagePreventionShield(target.getDamagePreventionShield() + prevent.amount());

            String logEntry = "The next " + prevent.amount() + " damage that would be dealt to " + target.getCard().getName() + " is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Prevention shield {} added to permanent {}", gameData.id, prevent.amount(), target.getCard().getName());
            return;
        }

        if (gameData.playerIds.contains(targetId)) {
            int currentShield = gameData.playerDamagePreventionShields.getOrDefault(targetId, 0);
            gameData.playerDamagePreventionShields.put(targetId, currentShield + prevent.amount());

            String playerName = gameData.playerIdToName.get(targetId);
            String logEntry = "The next " + prevent.amount() + " damage that would be dealt to " + playerName + " is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Prevention shield {} added to player {}", gameData.id, prevent.amount(), playerName);
        }
    }

    private void resolvePreventNextDamage(GameData gameData, PreventNextDamageEffect prevent) {
        gameData.globalDamagePreventionShield += prevent.amount();

        String logEntry = "The next " + prevent.amount() + " damage that would be dealt to any permanent or player is prevented.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Global prevention shield increased by {}", gameData.id, prevent.amount());
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

    private void resolveReturnSelfToHand(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<Card> hand = gameData.playerHands.get(controllerId);

        Permanent toReturn = null;
        for (Permanent p : battlefield) {
            if (p.getCard().getName().equals(entry.getCard().getName())) {
                toReturn = p;
                break;
            }
        }

        if (toReturn == null) {
            String logEntry = entry.getCard().getName() + " is no longer on the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        battlefield.remove(toReturn);
        gameHelper.removeOrphanedAuras(gameData);
        hand.add(toReturn.getOriginalCard());

        String logEntry = entry.getCard().getName() + " is returned to its owner's hand.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} returned to hand", gameData.id, entry.getCard().getName());
    }

    private void resolveReturnTargetPermanentToHand(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                gameHelper.removeOrphanedAuras(gameData);
                UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                gameData.stolenCreatures.remove(target.getId());
                List<Card> hand = gameData.playerHands.get(ownerId);
                hand.add(target.getOriginalCard());

                String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returned to owner's hand by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());

                break;
            }
        }
    }

    private void resolveReturnCreaturesToOwnersHand(GameData gameData, StackEntry entry, ReturnCreaturesToOwnersHandEffect bounce) {
        UUID controllerId = entry.getControllerId();
        Set<UUID> affectedPlayers = new HashSet<>();

        boolean controllerOnly = bounce.filters().stream().anyMatch(f -> f instanceof ControllerOnlyTargetFilter);
        boolean excludeSelf = bounce.filters().stream().anyMatch(f -> f instanceof ExcludeSelfTargetFilter);

        List<UUID> playerIds = controllerOnly
                ? List.of(controllerId)
                : gameData.orderedPlayerIds;

        for (UUID playerId : playerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }

            List<Permanent> creaturesToReturn = battlefield.stream()
                    .filter(p -> gameQueryService.isCreature(gameData, p))
                    .filter(p -> !excludeSelf || !p.getOriginalCard().getId().equals(entry.getCard().getId()))
                    .toList();

            for (Permanent creature : creaturesToReturn) {
                battlefield.remove(creature);
                UUID ownerId = gameData.stolenCreatures.getOrDefault(creature.getId(), playerId);
                gameData.stolenCreatures.remove(creature.getId());
                List<Card> hand = gameData.playerHands.get(ownerId);
                hand.add(creature.getOriginalCard());
                affectedPlayers.add(ownerId);

                String logEntry = creature.getCard().getName() + " is returned to its owner's hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returned to owner's hand by {}", gameData.id, creature.getCard().getName(), entry.getCard().getName());
            }
        }

        if (!affectedPlayers.isEmpty()) {
            gameHelper.removeOrphanedAuras(gameData);
        }
    }

    private void resolveReturnArtifactsTargetPlayerOwnsToHand(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> artifactsToReturn = battlefield.stream()
                .filter(p -> p.getCard().getType() == CardType.ARTIFACT)
                .toList();

        if (artifactsToReturn.isEmpty()) {
            return;
        }

        for (Permanent artifact : artifactsToReturn) {
            battlefield.remove(artifact);
            List<Card> hand = gameData.playerHands.get(targetPlayerId);
            hand.add(artifact.getOriginalCard());

            String logEntry = artifact.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id, artifact.getCard().getName(), entry.getCard().getName());
        }

        gameHelper.removeOrphanedAuras(gameData);
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

    private void resolveMillByHandSize(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        int handSize = hand != null ? hand.size() : 0;
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (handSize == 0) {
            String logEntry = playerName + " has no cards in hand — mills nothing.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);

        int cardsToMill = Math.min(handSize, deck.size());
        for (int i = 0; i < cardsToMill; i++) {
            Card card = deck.removeFirst();
            graveyard.add(card);
        }

        String logEntry = playerName + " mills " + cardsToMill + " card" + (cardsToMill != 1 ? "s" : "") + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} mills {} cards (hand size)", gameData.id, playerName, cardsToMill);
    }

    private void resolveBounceOwnCreatureOnUpkeep(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        List<UUID> creatureIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)) {
                    creatureIds.add(p.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            String logEntry = playerName + " controls no creatures — nothing to return.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.permanentChoiceContext = new PermanentChoiceContext.BounceCreature(targetPlayerId);
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                "Choose a creature you control to return to its owner's hand.");
    }

    private void resolveMillTargetPlayer(GameData gameData, StackEntry entry, MillTargetPlayerEffect mill) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        int cardsToMill = Math.min(mill.count(), deck.size());
        for (int i = 0; i < cardsToMill; i++) {
            Card card = deck.removeFirst();
            graveyard.add(card);
        }

        String logEntry = playerName + " mills " + cardsToMill + " card" + (cardsToMill != 1 ? "s" : "") + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} mills {} cards", gameData.id, playerName, cardsToMill);
    }

    private void resolveShuffleGraveyardIntoLibrary(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (graveyard.isEmpty()) {
            String logEntry = playerName + "'s graveyard is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            Collections.shuffle(deck);
            return;
        }

        int count = graveyard.size();
        deck.addAll(graveyard);
        graveyard.clear();
        Collections.shuffle(deck);

        String logEntry = playerName + " shuffles their graveyard (" + count + " card" + (count != 1 ? "s" : "") + ") into their library.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} shuffles graveyard ({} cards) into library", gameData.id, playerName, count);
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

    private void resolvePreventAllCombatDamage(GameData gameData) {
        gameData.preventAllCombatDamage = true;

        String logEntry = "All combat damage will be prevented this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }

    private void resolvePreventDamageFromColors(GameData gameData, PreventDamageFromColorsEffect effect) {
        gameData.preventDamageFromColors.addAll(effect.colors());

        String colorNames = effect.colors().stream()
                .map(c -> c.name().toLowerCase())
                .sorted()
                .reduce((a, b) -> a + " and " + b)
                .orElse("");
        String logEntry = "All damage from " + colorNames + " sources will be prevented this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }

    private void resolvePreventNextColorDamageToController(GameData gameData, StackEntry entry, PreventNextColorDamageToControllerEffect effect) {
        CardColor chosenColor = effect.chosenColor();
        if (chosenColor == null) return;

        UUID controllerId = entry.getControllerId();
        gameData.playerColorDamagePreventionCount
                .computeIfAbsent(controllerId, k -> new ConcurrentHashMap<>())
                .merge(chosenColor, 1, Integer::sum);
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

    private void resolveReturnAuraFromGraveyardToBattlefield(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetPermanentId());
        if (auraCard == null || !auraCard.isAura()) {
            String fizzleLog = entry.getDescription() + " fizzles (target Aura no longer in graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        List<Permanent> controllerBf = gameData.playerBattlefields.get(controllerId);
        List<UUID> creatureIds = new ArrayList<>();
        if (controllerBf != null) {
            for (Permanent p : controllerBf) {
                if (gameQueryService.isCreature(gameData, p)) {
                    creatureIds.add(p.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            String fizzleLog = entry.getDescription() + " fizzles (no creatures to attach Aura to).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        gameHelper.removeCardFromGraveyardById(gameData, auraCard.getId());
        gameData.pendingAuraCard = auraCard;

        playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds, "Choose a creature you control to attach " + auraCard.getName() + " to.");
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

    private void resolveReturnArtifactOrCreatureFromAnyGraveyardToBattlefield(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> cardPool = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.getType() == CardType.CREATURE || card.getType() == CardType.ARTIFACT) {
                    cardPool.add(card);
                }
            }
        }

        if (cardPool.isEmpty()) {
            String logEntry = entry.getDescription() + " — no artifact or creature cards in any graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            // Per Magic rules: spell fizzles when it has no legal targets at resolution.
            // Remove ShuffleIntoLibraryEffect so the card goes to graveyard instead of being shuffled.
            entry.getEffectsToResolve().removeIf(e -> e instanceof ShuffleIntoLibraryEffect);
            return;
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < cardPool.size(); i++) {
            indices.add(i);
        }

        gameData.graveyardChoiceCardPool = cardPool;
        gameData.graveyardChoiceDestination = GraveyardChoiceDestination.BATTLEFIELD;
        playerInputService.beginGraveyardChoice(gameData, controllerId, indices,
                "Choose an artifact or creature card from a graveyard to put onto the battlefield under your control.");
    }

    private void resolveReturnCardFromGraveyardToZone(GameData gameData, StackEntry entry,
            CardType cardType, GraveyardChoiceDestination destination, String prompt) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String typeName = cardType.name().toLowerCase();

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + typeName + " cards in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getType() == cardType) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + typeName + " cards in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.graveyardChoiceDestination = destination;
        playerInputService.beginGraveyardChoice(gameData, controllerId, matchingIndices, prompt);
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

    private void resolveRevealTopCardOfLibrary(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            Card topCard = deck.getFirst();
            String logEntry = playerName + " reveals " + topCard.getName() + " from the top of their library.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        log.info("Game {} - {} reveals top card of library", gameData.id, playerName);
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

    private void resolveCounterSpell(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetPermanentId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter target no longer on stack", gameData.id);
            return;
        }

        gameData.stack.remove(targetEntry);

        UUID ownerId = targetEntry.getControllerId();
        gameData.playerGraveyards.get(ownerId).add(targetEntry.getCard());

        String logMsg = targetEntry.getCard().getName() + " is countered.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} countered {}", gameData.id, entry.getCard().getName(), targetEntry.getCard().getName());
    }

    private void resolveCounterUnlessPays(GameData gameData, StackEntry entry, CounterUnlessPaysEffect effect) {
        UUID targetCardId = entry.getTargetPermanentId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter-unless-pays target no longer on stack", gameData.id);
            return;
        }

        UUID targetControllerId = targetEntry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(targetControllerId);
        ManaCost cost = new ManaCost("{" + effect.amount() + "}");

        if (!cost.canPay(pool)) {
            // Can't pay — counter immediately
            gameData.stack.remove(targetEntry);
            gameData.playerGraveyards.get(targetControllerId).add(targetEntry.getCard());

            String logMsg = targetEntry.getCard().getName() + " is countered.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} countered {} (can't pay {})", gameData.id, entry.getCard().getName(), targetEntry.getCard().getName(), effect.amount());
        } else {
            // Can pay — ask the opponent via the may ability system
            String prompt = "Pay {" + effect.amount() + "} to prevent " + targetEntry.getCard().getName() + " from being countered?";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), targetControllerId, List.of(effect), prompt, targetCardId
            ));
            // processNextMayAbility (called by resolveTopOfStack) will set awaitingInput and send the message
        }
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

    private void resolveReorderTopCardsOfLibrary(GameData gameData, StackEntry entry, ReorderTopCardsOfLibraryEffect reorder) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        int count = Math.min(reorder.count(), deck.size());
        if (count == 0) {
            String logMsg = entry.getCard().getName() + ": library is empty, nothing to reorder.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        if (count == 1) {
            String logMsg = gameData.playerIdToName.get(controllerId) + " looks at the top card of their library.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, count));

        gameData.awaitingLibraryReorderPlayerId = controllerId;
        gameData.awaitingLibraryReorderCards = topCards;
        gameData.awaitingInput = AwaitingInput.LIBRARY_REORDER;

        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ReorderLibraryCardsMessage(
                cardViews,
                "Put these cards back on top of your library in any order (top to bottom)."
        ));

        String logMsg = gameData.playerIdToName.get(controllerId) + " looks at the top " + count + " cards of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} reordering top {} cards of library", gameData.id, gameData.playerIdToName.get(controllerId), count);
    }

    private void resolveDealDamageToFlyingAndPlayers(GameData gameData, StackEntry entry) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logMsg = entry.getCard().getName() + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        int damage = entry.getXValue();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            Set<Integer> deadIndices = new TreeSet<>(Collections.reverseOrder());
            for (int i = 0; i < battlefield.size(); i++) {
                Permanent p = battlefield.get(i);
                if (gameQueryService.hasKeyword(gameData, p, Keyword.FLYING)) {
                    if (gameQueryService.hasProtectionFrom(gameData, p, entry.getCard().getColor())) {
                        continue;
                    }
                    int effectiveDamage = gameHelper.applyCreaturePreventionShield(gameData, p, damage);
                    int toughness = gameQueryService.getEffectiveToughness(gameData, p);
                    if (effectiveDamage >= toughness
                            && !gameQueryService.hasKeyword(gameData, p, Keyword.INDESTRUCTIBLE)
                            && !gameHelper.tryRegenerate(gameData, p)) {
                        deadIndices.add(i);
                    }
                }
            }

            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            for (int idx : deadIndices) {
                String playerName = gameData.playerIdToName.get(playerId);
                Permanent dead = battlefield.get(idx);
                String logEntry = playerName + "'s " + dead.getCard().getName() + " is destroyed by Hurricane.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                graveyard.add(dead.getOriginalCard());
                gameHelper.collectDeathTrigger(gameData, dead.getCard(), playerId, true);
                battlefield.remove(idx);
            }
        }

        gameHelper.removeOrphanedAuras(gameData);

        String cardName = entry.getCard().getName();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (gameHelper.applyColorDamagePreventionForPlayer(gameData, playerId, entry.getCard().getColor())) {
                continue;
            }
            int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, playerId, damage);
            effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);
            int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);

            if (effectiveDamage > 0) {
                String playerName = gameData.playerIdToName.get(playerId);
                String logEntry = playerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }

        gameHelper.checkWinCondition(gameData);
    }

    private void resolveLookAtTopCardsHandTopBottom(GameData gameData, StackEntry entry, LookAtTopCardsHandTopBottomEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        int count = Math.min(effect.count(), deck.size());
        if (count == 0) {
            String logMsg = entry.getCard().getName() + ": " + playerName + "'s library is empty, nothing to look at.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        if (count == 1) {
            // Only 1 card: it goes to hand
            Card card = deck.remove(0);
            gameData.playerHands.get(controllerId).add(card);
            String logMsg = playerName + " looks at the top card of their library and puts it into their hand.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        // Remove the top cards from the deck temporarily
        deck.subList(0, count).clear();

        gameData.awaitingHandTopBottomPlayerId = controllerId;
        gameData.awaitingHandTopBottomCards = topCards;
        gameData.awaitingInput = AwaitingInput.HAND_TOP_BOTTOM_CHOICE;

        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseHandTopBottomMessage(
                cardViews,
                "Look at the top " + count + " cards of your library. Choose one to put into your hand."
        ));

        String logMsg = playerName + " looks at the top " + count + " cards of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} resolving {} with {} cards", gameData.id, playerName, entry.getCard().getName(), count);
    }

    private void resolveSearchLibraryForBasicLandToHand(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> basicLands = new ArrayList<>();
        for (Card card : deck) {
            if (card.getType() == CardType.BASIC_LAND) {
                basicLands.add(card);
            }
        }

        if (basicLands.isEmpty()) {
            Collections.shuffle(deck);
            String logMsg = playerName + " searches their library but finds no basic land cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} searches library, no basic lands found", gameData.id, playerName);
            return;
        }

        gameData.awaitingLibrarySearchPlayerId = controllerId;
        gameData.awaitingLibrarySearchCards = basicLands;
        gameData.awaitingInput = AwaitingInput.LIBRARY_SEARCH;

        List<CardView> cardViews = basicLands.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ChooseCardFromLibraryMessage(
                cardViews,
                "Search your library for a basic land card to put into your hand."
        ));

        String logMsg = playerName + " searches their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} searching library for a basic land ({} found)", gameData.id, playerName, basicLands.size());
    }

    private void resolveExileCardsFromGraveyard(GameData gameData, StackEntry entry, ExileCardsFromGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Exile targeted cards that are still in graveyards
        if (targetCardIds != null && !targetCardIds.isEmpty()) {
            List<String> exiledNames = new ArrayList<>();
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null) {
                    exiledNames.add(card.getName());
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Card> graveyard = gameData.playerGraveyards.get(pid);
                        if (graveyard != null && graveyard.removeIf(c -> c.getId().equals(cardId))) {
                            gameData.playerExiledCards.get(pid).add(card);
                            break;
                        }
                    }
                }
            }
            if (!exiledNames.isEmpty()) {
                String logEntry = playerName + " exiles " + String.join(", ", exiledNames) + " from graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} exiled {} cards from graveyards", gameData.id, playerName, exiledNames.size());
            }
        }

        // Gain life after exile
        if (effect.lifeGain() > 0) {
            resolveGainLife(gameData, controllerId, effect.lifeGain());
        }
    }
}
