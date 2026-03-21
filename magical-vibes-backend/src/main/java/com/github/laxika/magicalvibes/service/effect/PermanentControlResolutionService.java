package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfBySlimeCountersOnLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenFromHalfLifeTotalAndDealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.CreateLifeTotalAvatarTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControllerLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerControlledCreatureSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerControlledLandSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerCreatureCardInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerOwnCreatureDeathsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfExiledCostCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerEquipmentOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerOpponentPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentWhileSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.AttachTargetToSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutSlimeCounterAndCreateOozeTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.scryfall.ScryfallOracleLoader;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentControlResolutionService {

    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final CreatureControlService creatureControlService;

    @HandlesEffect(CreateCreatureTokenEffect.class)
    private void resolveCreateCreatureToken(GameData gameData, StackEntry entry, CreateCreatureTokenEffect effect) {
        applyCreateCreatureToken(gameData, entry.getControllerId(), effect, entry.getCard().getSetCode());
    }

    @HandlesEffect(PutSlimeCounterAndCreateOozeTokenEffect.class)
    private void resolvePutSlimeCounterAndCreateOozeToken(GameData gameData, StackEntry entry) {
        UUID sourcePermId = entry.getSourcePermanentId();
        if (sourcePermId == null) {
            log.warn("Game {} - PutSlimeCounterAndCreateOozeTokenEffect has no sourcePermanentId", gameData.id);
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermId);
        if (source == null) {
            log.info("Game {} - Gutter Grime no longer on battlefield, effect fizzles", gameData.id);
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, source)) {
            return;
        }

        // Put a slime counter on the source
        source.setSlimeCounters(source.getSlimeCounters() + 1);
        int slimeCount = source.getSlimeCounters();

        String counterLog = source.getCard().getName() + " gets a slime counter (" + slimeCount + " total).";
        gameBroadcastService.logAndBroadcast(gameData, counterLog);
        log.info("Game {} - {} gets a slime counter ({} total)", gameData.id, source.getCard().getName(), slimeCount);

        // Create a 0/0 green Ooze token with a CDA linking to this Gutter Grime
        CreateCreatureTokenEffect tokenEffect = new CreateCreatureTokenEffect(
                1, "Ooze", 0, 0,
                CardColor.GREEN, List.of(CardSubtype.OOZE),
                Set.of(), Set.of(),
                Map.of(EffectSlot.STATIC, new BoostSelfBySlimeCountersOnLinkedPermanentEffect(sourcePermId))
        );
        applyCreateCreatureToken(gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
    }

    @HandlesEffect(CreateXCreatureTokenEffect.class)
    private void resolveCreateXCreatureToken(GameData gameData, StackEntry entry, CreateXCreatureTokenEffect effect) {
        int amount = entry.getXValue();
        if (amount <= 0) return;
        CreateCreatureTokenEffect tokenEffect = new CreateCreatureTokenEffect(
                amount, effect.tokenName(), effect.power(), effect.toughness(),
                effect.color(), effect.subtypes(), effect.keywords(), effect.additionalTypes()
        );
        applyCreateCreatureToken(gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
    }

    @HandlesEffect(CreateTokensPerOwnCreatureDeathsThisTurnEffect.class)
    private void resolveCreateTokensPerOwnCreatureDeathsThisTurn(GameData gameData, StackEntry entry,
                                                                   CreateTokensPerOwnCreatureDeathsThisTurnEffect effect) {
        int amount = gameData.creatureDeathCountThisTurn.getOrDefault(entry.getControllerId(), 0);
        if (amount <= 0) return;
        CreateCreatureTokenEffect tokenEffect = new CreateCreatureTokenEffect(
                amount, effect.tokenName(), effect.power(), effect.toughness(),
                effect.color(), effect.subtypes(), effect.keywords(), effect.additionalTypes()
        );
        applyCreateCreatureToken(gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
    }

    @HandlesEffect(CreateTokensPerCreatureCardInGraveyardEffect.class)
    private void resolveCreateTokensPerCreatureCardInGraveyard(GameData gameData, StackEntry entry,
                                                                CreateTokensPerCreatureCardInGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        int creatureCount = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.hasType(CardType.CREATURE)) {
                    creatureCount++;
                }
            }
        }
        if (creatureCount <= 0) return;
        CreateCreatureTokenEffect tokenEffect = new CreateCreatureTokenEffect(
                creatureCount, effect.tokenName(), effect.power(), effect.toughness(),
                effect.color(), null, effect.subtypes(), effect.keywords(), effect.additionalTypes(),
                effect.tappedAndAttacking(), false, Map.of(), false
        );
        applyCreateCreatureToken(gameData, controllerId, tokenEffect, entry.getCard().getSetCode());
    }

    @HandlesEffect(CreateTokensEqualToChargeCountersOnSourceEffect.class)
    private void resolveCreateTokensEqualToChargeCounters(GameData gameData, StackEntry entry,
                                                          CreateTokensEqualToChargeCountersOnSourceEffect effect) {
        int count = entry.getXValue();
        if (count <= 0) return;
        CreateCreatureTokenEffect tokenEffect = new CreateCreatureTokenEffect(
                count, effect.tokenName(), effect.power(), effect.toughness(),
                effect.color(), effect.subtypes(), effect.keywords(), effect.additionalTypes()
        );
        applyCreateCreatureToken(gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
    }

    @HandlesEffect(CreateTokensEqualToControlledCreatureCountEffect.class)
    private void resolveCreateTokensEqualToControlledCreatureCount(GameData gameData, StackEntry entry,
                                                                    CreateTokensEqualToControlledCreatureCountEffect effect) {
        int count = 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm)) {
                    count++;
                }
            }
        }
        if (count <= 0) return;

        CreateCreatureTokenEffect tokenEffect = new CreateCreatureTokenEffect(
                count, effect.tokenName(), effect.power(), effect.toughness(),
                effect.color(), effect.subtypes(), effect.keywords(), effect.additionalTypes()
        );
        applyCreateCreatureToken(gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
    }

    @HandlesEffect(CreateTokensPerControlledLandSubtypeEffect.class)
    private void resolveCreateTokensPerControlledLandSubtype(GameData gameData, StackEntry entry,
                                                              CreateTokensPerControlledLandSubtypeEffect effect) {
        int count = 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (perm.getCard().getSubtypes().contains(effect.landSubtype())) {
                    count++;
                }
            }
        }
        if (count <= 0) return;

        CreateCreatureTokenEffect tokenEffect = new CreateCreatureTokenEffect(
                count, effect.tokenName(), effect.power(), effect.toughness(),
                effect.color(), effect.subtypes(), effect.keywords(), effect.additionalTypes()
        );
        applyCreateCreatureToken(gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
    }

    @HandlesEffect(CreateTokensPerControlledCreatureSubtypeEffect.class)
    private void resolveCreateTokensPerControlledCreatureSubtype(GameData gameData, StackEntry entry,
                                                                  CreateTokensPerControlledCreatureSubtypeEffect effect) {
        int count = 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm)
                        && perm.getCard().getSubtypes().contains(effect.subtype())) {
                    count++;
                }
            }
        }
        int tokenCount = count / effect.divisor();
        if (tokenCount <= 0) return;

        CreateCreatureTokenEffect tokenEffect = new CreateCreatureTokenEffect(
                tokenCount, effect.tokenName(), effect.power(), effect.toughness(),
                effect.color(), effect.subtypes(), effect.keywords(), effect.additionalTypes()
        );
        applyCreateCreatureToken(gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
    }

    @HandlesEffect(ReturnSelfToHandAndCreateTokensEffect.class)
    private void resolveReturnSelfToHandAndCreateTokens(GameData gameData, StackEntry entry,
                                                         ReturnSelfToHandAndCreateTokensEffect effect) {
        // Try to return source to hand; if it already left the battlefield, skip the bounce
        // but still create tokens — the token creation is not contingent on the return.
        Permanent toReturn = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (toReturn != null) {
            permanentRemovalService.removePermanentToHand(gameData, toReturn);
            permanentRemovalService.removeOrphanedAuras(gameData);

            String logEntry = entry.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to hand", gameData.id, entry.getCard().getName());
        } else {
            String logEntry = entry.getCard().getName() + " is no longer on the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        applyCreateCreatureToken(gameData, entry.getControllerId(), effect.tokenEffect(), entry.getCard().getSetCode());
    }

    @HandlesEffect(SacrificeEnchantedCreatureAndCreateTokenEffect.class)
    private void resolveSacrificeEnchantedCreatureAndCreateToken(GameData gameData, StackEntry entry,
                                                                  SacrificeEnchantedCreatureAndCreateTokenEffect effect) {
        // Find the aura permanent via sourcePermanentId
        Permanent auraPerm = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (auraPerm == null) {
            log.info("Game {} - Aura {} no longer on battlefield, skipping sacrifice trigger",
                    gameData.id, entry.getCard().getName());
            return;
        }

        // Find the enchanted creature
        UUID enchantedId = auraPerm.getAttachedTo();
        if (enchantedId == null) {
            log.info("Game {} - {} is not attached to anything, skipping sacrifice trigger",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchantedCreature == null) {
            log.info("Game {} - Enchanted creature no longer on battlefield, skipping sacrifice",
                    gameData.id);
            return;
        }

        // Sacrifice the enchanted creature (its controller sacrifices it)
        String sacrificeLog = enchantedCreature.getCard().getName() + " is sacrificed ("
                + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, sacrificeLog);
        log.info("Game {} - {} sacrificed by {}", gameData.id,
                enchantedCreature.getCard().getName(), entry.getCard().getName());

        permanentRemovalService.removePermanentToGraveyard(gameData, enchantedCreature);
        permanentRemovalService.removeOrphanedAuras(gameData);

        // Create token for the aura's controller
        applyCreateCreatureToken(gameData, entry.getControllerId(), effect.tokenEffect(), entry.getCard().getSetCode());
    }

    @HandlesEffect(SacrificeEnchantedCreatureEffect.class)
    private void resolveSacrificeEnchantedCreature(GameData gameData, StackEntry entry,
                                                    SacrificeEnchantedCreatureEffect effect) {
        // Find the aura permanent via sourcePermanentId
        Permanent auraPerm = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (auraPerm == null) {
            log.info("Game {} - Aura {} no longer on battlefield, skipping sacrifice trigger",
                    gameData.id, entry.getCard().getName());
            return;
        }

        // Find the enchanted creature
        UUID enchantedId = auraPerm.getAttachedTo();
        if (enchantedId == null) {
            log.info("Game {} - {} is not attached to anything, skipping sacrifice trigger",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchantedCreature == null) {
            log.info("Game {} - Enchanted creature no longer on battlefield, skipping sacrifice",
                    gameData.id);
            return;
        }

        // Sacrifice the enchanted creature (its controller sacrifices it)
        String sacrificeLog = enchantedCreature.getCard().getName() + " is sacrificed ("
                + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, sacrificeLog);
        log.info("Game {} - {} sacrificed by {}", gameData.id,
                enchantedCreature.getCard().getName(), entry.getCard().getName());

        permanentRemovalService.removePermanentToGraveyard(gameData, enchantedCreature);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    public void applyCreateCreatureToken(GameData gameData, UUID controllerId, CreateCreatureTokenEffect token, String sourceSetCode) {
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
        int totalAmount = token.amount() * tokenMultiplier;
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        for (int i = 0; i < totalAmount; i++) {
            Card tokenCard = new Card();
            tokenCard.setName(token.tokenName());
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setManaCost("");
            tokenCard.setToken(true);
            tokenCard.setColor(token.color());
            tokenCard.setPower(token.power());
            tokenCard.setToughness(token.toughness());
            tokenCard.setSubtypes(token.subtypes());
            if (token.keywords() != null && !token.keywords().isEmpty()) {
                tokenCard.setKeywords(token.keywords());
            }
            if (token.additionalTypes() != null && !token.additionalTypes().isEmpty()) {
                tokenCard.setAdditionalTypes(token.additionalTypes());
            }
            if (token.tokenEffects() != null) {
                for (Map.Entry<EffectSlot, CardEffect> tokenEffect : token.tokenEffects().entrySet()) {
                    tokenCard.addEffect(tokenEffect.getKey(), tokenEffect.getValue());
                }
            }

            // Look up token image from Scryfall token set
            ScryfallOracleLoader.TokenImageData imageData = ScryfallOracleLoader.getTokenImage(
                    sourceSetCode, token.tokenName(), token.power(), token.toughness(), token.color()
            );
            if (imageData != null) {
                tokenCard.setSetCode(imageData.setCode());
                tokenCard.setCollectorNumber(imageData.collectorNumber());
            }

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

            if (token.tappedAndAttacking()) {
                tokenPermanent.tap();
                tokenPermanent.setAttacking(true);
            } else if (token.tapped()) {
                tokenPermanent.tap();
            }

            if (token.exileAtEndOfCombat()) {
                gameData.pendingTokenExilesAtEndOfCombat.add(tokenPermanent.getId());
            }

            String colorDesc;
            if (token.colors() != null && !token.colors().isEmpty()) {
                colorDesc = token.colors().stream()
                        .map(c -> c.name().charAt(0) + c.name().substring(1).toLowerCase())
                        .reduce((a, b) -> a + " and " + b).orElse("");
                colorDesc += " ";
            } else {
                colorDesc = "";
            }
            String tappedAttackingDesc = token.tappedAndAttacking() ? " tapped and attacking" : (token.tapped() ? " tapped" : "");
            String logEntry = "A " + token.power() + "/" + token.toughness() + " " + colorDesc + token.tokenName() + " creature token enters the battlefield" + tappedAttackingDesc + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
            if (!gameData.interaction.isAwaitingInput()) {
                legendRuleService.checkLegendRule(gameData, controllerId);
            }
        }

        log.info("Game {} - {} {} token(s) created for player {}", gameData.id, totalAmount, token.tokenName(), controllerId);
    }

    @HandlesEffect(CreateTokenFromHalfLifeTotalAndDealDamageEffect.class)
    private void resolveCreateTokenFromHalfLifeTotal(GameData gameData, StackEntry entry,
                                                      CreateTokenFromHalfLifeTotalAndDealDamageEffect effect) {
        UUID controllerId = entry.getControllerId();
        int currentLife = gameData.getLife(controllerId);
        int x = (currentLife + 1) / 2; // half life total, rounded up
        if (x < 0) x = 0;

        // Create the X/X token
        Card tokenCard = new Card();
        tokenCard.setName(effect.tokenName());
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(effect.color());
        tokenCard.setPower(x);
        tokenCard.setToughness(x);
        tokenCard.setSubtypes(effect.subtypes());

        ScryfallOracleLoader.TokenImageData imageData = ScryfallOracleLoader.getTokenImage(
                entry.getCard().getSetCode(), effect.tokenName(), x, x, effect.color()
        );
        if (imageData != null) {
            tokenCard.setSetCode(imageData.setCode());
            tokenCard.setCollectorNumber(imageData.collectorNumber());
        }

        Permanent tokenPerm = new Permanent(tokenCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPerm);

        String tokenLog = "A " + x + "/" + x + " black " + effect.tokenName() + " creature token enters the battlefield.";
        gameBroadcastService.logAndBroadcast(gameData, tokenLog);
        log.info("Game {} - {} {}/{} token created for {}", gameData.id, effect.tokenName(), x, x, controllerId);

        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);

        // The token deals X damage to the controller (damage source is the token, not the Saga)
        if (x > 0) {
            if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
                String playerName = gameData.playerIdToName.get(controllerId);
                gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            } else {
                int life = gameData.getLife(controllerId);
                gameData.playerLifeTotals.put(controllerId, life - x);
                String dmgLog = effect.tokenName() + " deals " + x + " damage to " + gameData.playerIdToName.get(controllerId) + ".";
                gameBroadcastService.logAndBroadcast(gameData, dmgLog);
                log.info("Game {} - {} deals {} damage to controller {}", gameData.id, effect.tokenName(), x, controllerId);
                triggerCollectionService.checkLifeLossTriggers(gameData, controllerId, x);
            }
        }
    }

    @HandlesEffect(CreateTokenPerEquipmentOnSourceEffect.class)
    private void resolveCreateTokenPerEquipmentOnSource(GameData gameData, StackEntry entry, CreateTokenPerEquipmentOnSourceEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            log.warn("Game {} - CreateTokenPerEquipmentOnSource requires sourcePermanentId", gameData.id);
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            log.info("Game {} - Source permanent no longer on battlefield, skipping token creation", gameData.id);
            return;
        }

        // Count Equipment attached to the source permanent
        int equipmentCount = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                        && p.isAttached()
                        && p.getAttachedTo().equals(sourcePermanentId)) {
                    equipmentCount++;
                }
            }
        }

        if (equipmentCount == 0) {
            log.info("Game {} - No Equipment attached to {}, no tokens created", gameData.id, entry.getCard().getName());
            return;
        }

        CreateCreatureTokenEffect tokenEffect = new CreateCreatureTokenEffect(
                equipmentCount, effect.tokenName(), effect.power(), effect.toughness(),
                effect.color(), effect.subtypes(), effect.keywords(), effect.additionalTypes()
        );
        applyCreateCreatureToken(gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
    }

    @HandlesEffect(CreateTokenPerOpponentPoisonCounterEffect.class)
    private void resolveCreateTokenPerOpponentPoisonCounter(GameData gameData, StackEntry entry, CreateTokenPerOpponentPoisonCounterEffect effect) {
        UUID controllerId = entry.getControllerId();

        // Count total poison counters on all opponents
        int totalPoison = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)) {
                totalPoison += gameData.playerPoisonCounters.getOrDefault(playerId, 0);
            }
        }

        if (totalPoison == 0) {
            log.info("Game {} - No poison counters on opponents, no tokens created", gameData.id);
            return;
        }

        CreateCreatureTokenEffect tokenEffect = new CreateCreatureTokenEffect(
                totalPoison, effect.tokenName(), effect.power(), effect.toughness(),
                effect.color(), effect.subtypes(), effect.keywords(), effect.additionalTypes()
        );
        applyCreateCreatureToken(gameData, controllerId, tokenEffect, entry.getCard().getSetCode());
    }

    @HandlesEffect(CreateLifeTotalAvatarTokenEffect.class)
    private void resolveCreateLifeTotalAvatarToken(GameData gameData, StackEntry entry, CreateLifeTotalAvatarTokenEffect effect) {
        UUID controllerId = entry.getControllerId();
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));

        for (int copy = 0; copy < tokenMultiplier; copy++) {
            Card tokenCard = new Card();
            tokenCard.setName(effect.tokenName());
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setManaCost("");
            tokenCard.setToken(true);
            tokenCard.setColor(effect.color());
            tokenCard.setPower(0);
            tokenCard.setToughness(0);
            tokenCard.setSubtypes(effect.subtypes());

            // CDA: "This creature's power and toughness are each equal to your life total."
            tokenCard.addEffect(EffectSlot.STATIC, new PowerToughnessEqualToControllerLifeTotalEffect());

            ScryfallOracleLoader.TokenImageData imageData = ScryfallOracleLoader.getTokenImage(
                    entry.getCard().getSetCode(), effect.tokenName(), 0, 0, effect.color()
            );
            if (imageData != null) {
                tokenCard.setSetCode(imageData.setCode());
                tokenCard.setCollectorNumber(imageData.collectorNumber());
            }

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

            int lifeTotal = gameData.playerLifeTotals.getOrDefault(controllerId, 0);
            String logEntry = entry.getCard().getName() + " creates a " + lifeTotal + "/" + lifeTotal
                    + " white " + effect.tokenName() + " creature token.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} creates a {}/{} {} token", gameData.id, entry.getCard().getName(),
                    lifeTotal, lifeTotal, effect.tokenName());
        }
    }

    @HandlesEffect(LivingWeaponEffect.class)
    private void resolveLivingWeapon(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));

        Permanent lastTokenPermanent = null;
        for (int copy = 0; copy < tokenMultiplier; copy++) {
            // Create a 0/0 black Phyrexian Germ creature token
            Card tokenCard = new Card();
            tokenCard.setName("Phyrexian Germ");
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setManaCost("");
            tokenCard.setToken(true);
            tokenCard.setColor(CardColor.BLACK);
            tokenCard.setPower(0);
            tokenCard.setToughness(0);
            tokenCard.setSubtypes(List.of(CardSubtype.PHYREXIAN, CardSubtype.GERM));

            // Look up token image from Scryfall token set
            // Scryfall names this token "Germ" (subtypes are Phyrexian Germ)
            ScryfallOracleLoader.TokenImageData germImageData = ScryfallOracleLoader.getTokenImage(
                    entry.getCard().getSetCode(), "Germ", 0, 0, CardColor.BLACK
            );
            if (germImageData != null) {
                tokenCard.setSetCode(germImageData.setCode());
                tokenCard.setCollectorNumber(germImageData.collectorNumber());
            }

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

            String logEntry = "A 0/0 black Phyrexian Germ creature token enters the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
            if (!gameData.interaction.isAwaitingInput()) {
                legendRuleService.checkLegendRule(gameData, controllerId);
            }

            lastTokenPermanent = tokenPermanent;
        }

        // Attach the equipment to the last token created (per CR 614.6b)
        if (lastTokenPermanent != null) {
            Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (equipment != null) {
                equipment.setAttachedTo(lastTokenPermanent.getId());
                String attachLog = entry.getCard().getName() + " is now attached to Phyrexian Germ.";
                gameBroadcastService.logAndBroadcast(gameData, attachLog);
                log.info("Game {} - {} attached to Phyrexian Germ token via living weapon", gameData.id, entry.getCard().getName());
            }
        }

        log.info("Game {} - Living weapon: {} Phyrexian Germ token(s) created for player {}", gameData.id, tokenMultiplier, controllerId);
    }

    @HandlesEffect(PutAuraFromHandOntoSelfEffect.class)
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

    @HandlesEffect(PutTargetOnBottomOfLibraryEffect.class)
    private void resolvePutTargetOnBottomOfLibrary(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        if (permanentRemovalService.removePermanentToLibraryBottom(gameData, target)) {
            String logEntry = target.getCard().getName() + " is put on the bottom of its owner's library.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} put on bottom of library", gameData.id, target.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    @HandlesEffect(PutTargetOnTopOfLibraryEffect.class)
    private void resolvePutTargetOnTopOfLibrary(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        if (permanentRemovalService.removePermanentToLibraryTop(gameData, target)) {
            String logEntry = target.getCard().getName() + " is put on top of its owner's library.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} put on top of library", gameData.id, target.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    @HandlesEffect(SacrificeAtEndOfCombatEffect.class)
    private void resolveSacrificeAtEndOfCombat(GameData gameData, StackEntry entry) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null) {
            gameData.permanentsToSacrificeAtEndOfCombat.add(self.getId());
            String logEntry = entry.getCard().getName() + " will be sacrificed at end of combat.";
            gameData.gameLog.add(logEntry);
        }
    }

    @HandlesEffect(SacrificeSelfEffect.class)
    private void resolveSacrificeSelf(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        if (permanentRemovalService.removePermanentToGraveyard(gameData, self)) {
            triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, entry.getControllerId());
            String logEntry = self.getCard().getName() + " is sacrificed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    @HandlesEffect(RedirectUnblockedCombatDamageToSelfEffect.class)
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

    @HandlesEffect(RegenerateEffect.class)
    private void resolveRegenerate(GameData gameData, StackEntry entry) {
        UUID regenerationTargetId = entry.getTargetId();
        if (regenerationTargetId == null && entry.getSourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                regenerationTargetId = source.getAttachedTo();
            }
        }

        Permanent perm = gameQueryService.findPermanentById(gameData, regenerationTargetId);
        if (perm == null) {
            return;
        }
        perm.setRegenerationShield(perm.getRegenerationShield() + 1);

        String logEntry = perm.getCard().getName() + " gains a regeneration shield.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gains a regeneration shield", gameData.id, perm.getCard().getName());
    }

    @HandlesEffect(RegenerateAllOwnCreaturesEffect.class)
    private void resolveRegenerateAllOwnCreatures(GameData gameData, StackEntry entry, RegenerateAllOwnCreaturesEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) return;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        int count = 0;
        for (Permanent perm : battlefield) {
            if (gameQueryService.isCreature(gameData, perm)
                    && (effect.filter() == null
                        || gameQueryService.matchesPermanentPredicate(perm, effect.filter(), filterContext))) {
                perm.setRegenerationShield(perm.getRegenerationShield() + 1);
                count++;
            }
        }

        if (count > 0) {
            String logEntry = count + " creature(s) gain a regeneration shield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} creature(s) gain regeneration shields", gameData.id, count);
        }
    }

    @HandlesEffect(GainControlOfTargetAuraEffect.class)
    private void resolveGainControlOfTargetAura(GameData gameData, StackEntry entry) {
        UUID casterId = entry.getControllerId();
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (aura == null) return;

        UUID currentControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if (currentControllerId != null && !currentControllerId.equals(casterId)) {
            gameData.playerBattlefields.get(currentControllerId).remove(aura);
            gameData.playerBattlefields.get(casterId).add(aura);
            String casterName = gameData.playerIdToName.get(casterId);
            String logEntry = casterName + " gains control of " + aura.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} gains control of {}", gameData.id, casterName, aura.getCard().getName());
        }

        List<UUID> validCreatureIds = new ArrayList<>();
        gameData.forEachPermanent((pid, p) -> {
            if (gameQueryService.isCreature(gameData, p) && !p.getId().equals(aura.getAttachedTo())) {
                validCreatureIds.add(p.getId());
            }
        });

        if (!validCreatureIds.isEmpty()) {
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.AuraGraft(aura.getId()));
            playerInputService.beginPermanentChoice(gameData, casterId, validCreatureIds,
                    "Attach " + aura.getCard().getName() + " to another permanent it can enchant.");
        } else {
            String logEntry = aura.getCard().getName() + " stays attached to its current target (no other valid permanents).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }
    }

    @HandlesEffect(GainControlOfEnchantedTargetEffect.class)
    private void resolveGainControlOfEnchantedTarget(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        if (!gameQueryService.isEnchanted(gameData, target)) {
            String logEntry = entry.getCard().getName() + "'s ability has no effect (" + target.getCard().getName() + " is not enchanted).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        creatureControlService.stealPermanent(gameData, entry.getControllerId(), target);
        gameData.enchantmentDependentStolenCreatures.add(target.getId());
    }

    @HandlesEffect(GainControlOfTargetPermanentUntilEndOfTurnEffect.class)
    private void resolveGainControlOfTargetPermanentUntilEndOfTurn(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        UUID oldController = gameQueryService.findPermanentController(gameData, target.getId());
        if (oldController == null || oldController.equals(entry.getControllerId())) {
            return;
        }

        creatureControlService.stealPermanent(gameData, entry.getControllerId(), target);
        gameData.untilEndOfTurnStolenCreatures.add(target.getId());
    }

    @HandlesEffect(GainControlOfTargetPermanentEffect.class)
    private void resolveGainControlOfTargetPermanent(GameData gameData, StackEntry entry, GainControlOfTargetPermanentEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        UUID oldController = gameQueryService.findPermanentController(gameData, target.getId());
        if (oldController != null && !oldController.equals(entry.getControllerId())) {
            creatureControlService.stealPermanent(gameData, entry.getControllerId(), target);
            gameData.permanentControlStolenCreatures.add(target.getId());
        }

        if (effect.grantedSubtype() != null && !target.getGrantedSubtypes().contains(effect.grantedSubtype())) {
            target.getGrantedSubtypes().add(effect.grantedSubtype());
            String subtypeLog = target.getCard().getName() + " becomes a " + effect.grantedSubtype().getDisplayName() + " in addition to its other types.";
            gameBroadcastService.logAndBroadcast(gameData, subtypeLog);
        }
    }

    @HandlesEffect(GainControlOfTargetPermanentWhileSourceEffect.class)
    private void resolveGainControlOfTargetPermanentWhileSource(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        // Per ruling: if you lose control of the source permanent before this resolves,
        // the ability resolves with no effect.
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            String logEntry = entry.getCard().getName() + "'s ability has no effect (source left the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        UUID sourceController = gameQueryService.findPermanentController(gameData, sourcePermanentId);
        if (sourceController == null || !sourceController.equals(entry.getControllerId())) {
            String logEntry = entry.getCard().getName() + "'s ability has no effect (controller no longer controls " + source.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        UUID oldController = gameQueryService.findPermanentController(gameData, target.getId());
        if (oldController != null && !oldController.equals(entry.getControllerId())) {
            creatureControlService.stealPermanent(gameData, entry.getControllerId(), target);
            gameData.sourceDependentStolenCreatures.put(target.getId(), sourcePermanentId);
        }
    }

    @HandlesEffect(GrantSubtypeToTargetCreatureEffect.class)
    private void resolveGrantSubtypeToTargetCreature(GameData gameData, StackEntry entry, GrantSubtypeToTargetCreatureEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        if (!target.getGrantedSubtypes().contains(effect.subtype())) {
            target.getGrantedSubtypes().add(effect.subtype());
            String subtypeLog = target.getCard().getName() + " becomes a " + effect.subtype().getDisplayName() + " in addition to its other types.";
            gameBroadcastService.logAndBroadcast(gameData, subtypeLog);
        }
    }

    @HandlesEffect(AttachTargetToSourcePermanentEffect.class)
    private void resolveAttachTargetToSourcePermanent(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) return;

        target.setAttachedTo(source.getId());
        String attachLog = target.getCard().getName() + " is attached to " + source.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, attachLog);
        log.info("Game {} - {} attached to {}", gameData.id, target.getCard().getName(), source.getCard().getName());
    }

    @HandlesEffect(TargetPlayerGainsControlOfSourceCreatureEffect.class)
    private void resolveTargetPlayerGainsControlOfSourceCreature(GameData gameData, StackEntry entry) {
        if (entry.getTargetId() == null || !gameData.playerIds.contains(entry.getTargetId())) {
            return;
        }

        UUID newControllerId = entry.getTargetId();
        Permanent source = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(entry.getCard().getId())) {
                    source = permanent;
                    break;
                }
            }
            if (source != null) {
                break;
            }
        }

        if (source == null) {
            String fizzleLog = entry.getCard().getName() + "'s ability has no effect (it is no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        creatureControlService.stealPermanent(gameData, newControllerId, source);
        gameData.permanentControlStolenCreatures.add(source.getId());
    }

    @HandlesEffect(CreateTokenCopyOfImprintedCardEffect.class)
    private void resolveCreateTokenCopyOfImprinted(GameData gameData, StackEntry entry, CreateTokenCopyOfImprintedCardEffect effect) {
        // Per rulings (Mimic Vat, Prototype Portal): if the source permanent has left the battlefield
        // by the time the ability resolves, the token is still created. The imprinted card reference
        // is preserved via entry.getCard() (a snapshot captured when the ability went on the stack).
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());

        Card imprintedCard = sourcePermanent != null
                ? sourcePermanent.getCard().getImprintedCard()
                : entry.getCard().getImprintedCard();
        if (imprintedCard == null) {
            log.info("Game {} - No card imprinted on {}, no token created", gameData.id, entry.getCard().getName());
            return;
        }

        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
        for (int copy = 0; copy < tokenMultiplier; copy++) {
            // Create a token that's a copy of the imprinted card (copying all copiable values)
            Card tokenCard = new Card();
            tokenCard.setName(imprintedCard.getName());
            tokenCard.setType(imprintedCard.getType());
            tokenCard.setAdditionalTypes(imprintedCard.getAdditionalTypes());
            tokenCard.setManaCost(imprintedCard.getManaCost() != null ? imprintedCard.getManaCost() : "");
            tokenCard.setToken(true);
            tokenCard.setColor(imprintedCard.getColor());
            tokenCard.setSupertypes(imprintedCard.getSupertypes());
            tokenCard.setPower(imprintedCard.getPower());
            tokenCard.setToughness(imprintedCard.getToughness());
            tokenCard.setSubtypes(imprintedCard.getSubtypes());
            tokenCard.setCardText(imprintedCard.getCardText());
            tokenCard.setSetCode(imprintedCard.getSetCode());
            tokenCard.setCollectorNumber(imprintedCard.getCollectorNumber());

            // Copy keywords (conditionally add haste)
            Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            if (imprintedCard.getKeywords() != null) {
                keywords.addAll(imprintedCard.getKeywords());
            }
            if (effect.grantHaste()) {
                keywords.add(Keyword.HASTE);
            }
            tokenCard.setKeywords(keywords);

            // Copy effects and activated abilities (copiable characteristics per CR 707.2)
            for (EffectSlot slot : EffectSlot.values()) {
                for (EffectRegistration reg : imprintedCard.getEffectRegistrations(slot)) {
                    tokenCard.addEffect(slot, reg.effect(), reg.triggerMode());
                }
            }
            for (ActivatedAbility ability : imprintedCard.getActivatedAbilities()) {
                tokenCard.addActivatedAbility(ability);
            }

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);

            // Conditionally schedule for exile at beginning of next end step
            if (effect.exileAtEndStep()) {
                gameData.pendingTokenExilesAtEndStep.add(tokenPermanent.getId());
            }

            String logMsg = effect.grantHaste()
                    ? "A token copy of " + imprintedCard.getName() + " is created with haste."
                    : "A token copy of " + imprintedCard.getName() + " is created.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - Token copy of {} created via {}", gameData.id, imprintedCard.getName(), sourcePermanent.getCard().getName());

            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, tokenPermanent.getId(), false);
        }
    }

    @HandlesEffect(CreateTokenCopyOfExiledCostCardEffect.class)
    private void resolveCreateTokenCopyOfExiledCostCard(GameData gameData, StackEntry entry) {
        resolveCreateTokenCopyOfImprinted(gameData, entry, new CreateTokenCopyOfImprintedCardEffect(false, false));
    }

    @HandlesEffect(CreateTokenCopyOfSourceEffect.class)
    private void resolveCreateTokenCopyOfSource(GameData gameData, StackEntry entry) {
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourcePermanent == null) {
            log.info("Game {} - Source permanent no longer on battlefield", gameData.id);
            return;
        }

        Card sourceCard = sourcePermanent.getCard();

        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
        for (int copy = 0; copy < tokenMultiplier; copy++) {
            // Create a token that's a copy of the source permanent (copying all copiable values per CR 707.2)
            Card tokenCard = new Card();
            tokenCard.setName(sourceCard.getName());
            tokenCard.setType(sourceCard.getType());
            tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
            tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
            tokenCard.setToken(true);
            tokenCard.setColor(sourceCard.getColor());
            tokenCard.setSupertypes(sourceCard.getSupertypes());
            tokenCard.setPower(sourceCard.getPower());
            tokenCard.setToughness(sourceCard.getToughness());
            tokenCard.setSubtypes(sourceCard.getSubtypes());
            tokenCard.setCardText(sourceCard.getCardText());
            tokenCard.setSetCode(sourceCard.getSetCode());
            tokenCard.setCollectorNumber(sourceCard.getCollectorNumber());

            // Copy keywords
            if (sourceCard.getKeywords() != null) {
                tokenCard.setKeywords(EnumSet.copyOf(sourceCard.getKeywords()));
            }

            // Copy effects and activated abilities (copiable characteristics per CR 707.2)
            for (EffectSlot slot : EffectSlot.values()) {
                for (EffectRegistration reg : sourceCard.getEffectRegistrations(slot)) {
                    tokenCard.addEffect(slot, reg.effect(), reg.triggerMode());
                }
            }
            for (ActivatedAbility ability : sourceCard.getActivatedAbilities()) {
                tokenCard.addActivatedAbility(ability);
            }

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);

            String logMsg = "A token copy of " + sourceCard.getName() + " is created.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - Token copy of {} created via {}", gameData.id, sourceCard.getName(), sourceCard.getName());

            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, tokenPermanent.getId(), false);
        }
    }

    @HandlesEffect(CreateTokenCopyOfTargetPermanentEffect.class)
    private void resolveCreateTokenCopyOfTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (targetPermanent == null) {
            log.info("Game {} - Target permanent no longer on battlefield, no token created", gameData.id);
            return;
        }

        Card sourceCard = targetPermanent.getCard();

        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
        for (int copy = 0; copy < tokenMultiplier; copy++) {
            // Create a token that's a copy of the target permanent (copying all copiable values per CR 707.2)
            Card tokenCard = new Card();
            tokenCard.setName(sourceCard.getName());
            tokenCard.setType(sourceCard.getType());
            tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
            tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
            tokenCard.setToken(true);
            tokenCard.setColor(sourceCard.getColor());
            tokenCard.setSupertypes(sourceCard.getSupertypes());
            tokenCard.setPower(sourceCard.getPower());
            tokenCard.setToughness(sourceCard.getToughness());
            tokenCard.setSubtypes(sourceCard.getSubtypes());
            tokenCard.setCardText(sourceCard.getCardText());
            tokenCard.setSetCode(sourceCard.getSetCode());
            tokenCard.setCollectorNumber(sourceCard.getCollectorNumber());

            // Copy keywords
            if (sourceCard.getKeywords() != null) {
                tokenCard.setKeywords(EnumSet.copyOf(sourceCard.getKeywords()));
            }

            // Copy effects and activated abilities (copiable characteristics per CR 707.2)
            for (EffectSlot slot : EffectSlot.values()) {
                for (EffectRegistration reg : sourceCard.getEffectRegistrations(slot)) {
                    tokenCard.addEffect(slot, reg.effect(), reg.triggerMode());
                }
            }
            for (ActivatedAbility ability : sourceCard.getActivatedAbilities()) {
                tokenCard.addActivatedAbility(ability);
            }

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);

            String logMsg = "A token copy of " + sourceCard.getName() + " is created.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - Token copy of {} created via Mirrorworks-like ability", gameData.id, sourceCard.getName());

            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, tokenPermanent.getId(), false);
        }
    }

    @HandlesEffect(CreateTokenCopyOfEquippedCreatureEffect.class)
    private void resolveCreateTokenCopyOfEquippedCreature(GameData gameData, StackEntry entry,
                                                          CreateTokenCopyOfEquippedCreatureEffect effect) {
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourcePermanent == null) {
            log.info("Game {} - Source equipment no longer on battlefield", gameData.id);
            return;
        }

        UUID equippedCreatureId = sourcePermanent.getAttachedTo();
        if (equippedCreatureId == null) {
            log.info("Game {} - Equipment is not attached to any creature", gameData.id);
            return;
        }

        Permanent equippedCreature = gameQueryService.findPermanentById(gameData, equippedCreatureId);
        if (equippedCreature == null) {
            log.info("Game {} - Equipped creature no longer on battlefield", gameData.id);
            return;
        }

        Card sourceCard = equippedCreature.getCard();

        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
        for (int copy = 0; copy < tokenMultiplier; copy++) {
            // Create a token that's a copy of the equipped creature (copying all copiable values per CR 707.2)
            Card tokenCard = new Card();
            tokenCard.setName(sourceCard.getName());
            tokenCard.setType(sourceCard.getType());
            tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
            tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
            tokenCard.setToken(true);
            tokenCard.setColor(sourceCard.getColor());
            tokenCard.setPower(sourceCard.getPower());
            tokenCard.setToughness(sourceCard.getToughness());
            tokenCard.setSubtypes(sourceCard.getSubtypes());
            tokenCard.setCardText(sourceCard.getCardText());
            tokenCard.setSetCode(sourceCard.getSetCode());
            tokenCard.setCollectorNumber(sourceCard.getCollectorNumber());

            // Handle supertypes: optionally remove LEGENDARY
            if (effect.removeLegendary() && sourceCard.getSupertypes().contains(CardSupertype.LEGENDARY)) {
                EnumSet<CardSupertype> modifiedSupertypes = EnumSet.copyOf(sourceCard.getSupertypes());
                modifiedSupertypes.remove(CardSupertype.LEGENDARY);
                tokenCard.setSupertypes(modifiedSupertypes);
            } else {
                tokenCard.setSupertypes(sourceCard.getSupertypes());
            }

            // Copy keywords, optionally adding haste
            if (sourceCard.getKeywords() != null && !sourceCard.getKeywords().isEmpty()) {
                EnumSet<Keyword> tokenKeywords = EnumSet.copyOf(sourceCard.getKeywords());
                if (effect.grantHaste()) {
                    tokenKeywords.add(Keyword.HASTE);
                }
                tokenCard.setKeywords(tokenKeywords);
            } else if (effect.grantHaste()) {
                tokenCard.setKeywords(EnumSet.of(Keyword.HASTE));
            }

            // Copy effects and activated abilities (copiable characteristics per CR 707.2)
            for (EffectSlot slot : EffectSlot.values()) {
                for (EffectRegistration reg : sourceCard.getEffectRegistrations(slot)) {
                    tokenCard.addEffect(slot, reg.effect(), reg.triggerMode());
                }
            }
            for (ActivatedAbility ability : sourceCard.getActivatedAbilities()) {
                tokenCard.addActivatedAbility(ability);
            }

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);

            String logMsg = "A token copy of " + sourceCard.getName() + " is created (non-legendary, with haste).";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - Token copy of {} created via Helm of the Host", gameData.id, sourceCard.getName());

            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, tokenPermanent.getId(), false);
        }
    }

}


