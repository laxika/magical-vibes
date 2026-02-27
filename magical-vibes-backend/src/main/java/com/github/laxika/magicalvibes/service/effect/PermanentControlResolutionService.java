package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerEquipmentOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEquipmentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.service.CreatureControlService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.LegendRuleService;
import com.github.laxika.magicalvibes.service.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentControlResolutionService {

    private final GameHelper gameHelper;
    private final LegendRuleService legendRuleService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final CreatureControlService creatureControlService;

    @HandlesEffect(CreateCreatureTokenEffect.class)
    private void resolveCreateCreatureToken(GameData gameData, StackEntry entry, CreateCreatureTokenEffect effect) {
        applyCreateCreatureToken(gameData, entry.getControllerId(), effect);
    }

    private void applyCreateCreatureToken(GameData gameData, UUID controllerId, CreateCreatureTokenEffect token) {
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(gameHelper.snapshotEnterTappedTypes(gameData));
        for (int i = 0; i < token.amount(); i++) {
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

            Permanent tokenPermanent = new Permanent(tokenCard);
            gameHelper.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

            String colorDesc;
            if (token.colors() != null && !token.colors().isEmpty()) {
                colorDesc = token.colors().stream()
                        .map(c -> c.name().charAt(0) + c.name().substring(1).toLowerCase())
                        .reduce((a, b) -> a + " and " + b).orElse("");
                colorDesc += " ";
            } else {
                colorDesc = "";
            }
            String logEntry = "A " + token.power() + "/" + token.toughness() + " " + colorDesc + token.tokenName() + " creature token enters the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
            if (!gameData.interaction.isAwaitingInput()) {
                legendRuleService.checkLegendRule(gameData, controllerId);
            }
        }

        log.info("Game {} - {} {} token(s) created for player {}", gameData.id, token.amount(), token.tokenName(), controllerId);
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
                        && p.getAttachedTo() != null
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
        applyCreateCreatureToken(gameData, entry.getControllerId(), tokenEffect);
    }

    @HandlesEffect(LivingWeaponEffect.class)
    private void resolveLivingWeapon(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

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

        Permanent tokenPermanent = new Permanent(tokenCard);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(gameHelper.snapshotEnterTappedTypes(gameData));
        gameHelper.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

        String logEntry = "A 0/0 black Phyrexian Germ creature token enters the battlefield.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        // Attach the equipment to the token
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (equipment != null) {
            equipment.setAttachedTo(tokenPermanent.getId());
            String attachLog = entry.getCard().getName() + " is now attached to Phyrexian Germ.";
            gameBroadcastService.logAndBroadcast(gameData, attachLog);
            log.info("Game {} - {} attached to Phyrexian Germ token via living weapon", gameData.id, entry.getCard().getName());
        }

        gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }

        log.info("Game {} - Living weapon: Phyrexian Germ token created for player {}", gameData.id, controllerId);
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
        UUID regenerationTargetId = entry.getTargetPermanentId();
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

    @HandlesEffect(GainControlOfTargetAuraEffect.class)
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
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        if (!gameQueryService.isEnchanted(gameData, target)) {
            String logEntry = entry.getCard().getName() + "'s ability has no effect (" + target.getCard().getName() + " is not enchanted).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        creatureControlService.stealCreature(gameData, entry.getControllerId(), target);
        gameData.enchantmentDependentStolenCreatures.add(target.getId());
    }

    @HandlesEffect(GainControlOfTargetCreatureUntilEndOfTurnEffect.class)
    private void resolveGainControlOfTargetCreatureUntilEndOfTurn(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        UUID oldController = findControllerId(gameData, target);
        if (oldController == null || oldController.equals(entry.getControllerId())) {
            return;
        }

        creatureControlService.stealCreature(gameData, entry.getControllerId(), target);
        gameData.untilEndOfTurnStolenCreatures.add(target.getId());
    }

    @HandlesEffect(GainControlOfTargetEquipmentUntilEndOfTurnEffect.class)
    private void resolveGainControlOfTargetEquipmentUntilEndOfTurn(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        UUID oldController = findControllerId(gameData, target);
        if (oldController == null || oldController.equals(entry.getControllerId())) {
            return;
        }

        // Gain control of the equipment until end of turn
        creatureControlService.stealCreature(gameData, entry.getControllerId(), target);
        gameData.untilEndOfTurnStolenCreatures.add(target.getId());

        // Attach it to the source creature
        Permanent sourceCreature = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourceCreature != null) {
            target.setAttachedTo(sourceCreature.getId());
            String attachLog = target.getCard().getName() + " is attached to " + sourceCreature.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, attachLog);
            log.info("Game {} - {} attached to {} via Ogre Geargrabber ability",
                    gameData.id, target.getCard().getName(), sourceCreature.getCard().getName());
        }
    }

    @HandlesEffect(TargetPlayerGainsControlOfSourceCreatureEffect.class)
    private void resolveTargetPlayerGainsControlOfSourceCreature(GameData gameData, StackEntry entry) {
        if (entry.getTargetPermanentId() == null || !gameData.playerIds.contains(entry.getTargetPermanentId())) {
            return;
        }

        UUID newControllerId = entry.getTargetPermanentId();
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

        creatureControlService.stealCreature(gameData, newControllerId, source);
        gameData.permanentControlStolenCreatures.add(source.getId());
    }

    private UUID findControllerId(GameData gameData, Permanent permanent) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield != null && battlefield.contains(permanent)) {
                return pid;
            }
        }
        return null;
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
        gameHelper.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);

        // Conditionally schedule for exile at beginning of next end step
        if (effect.exileAtEndStep()) {
            gameData.pendingTokenExilesAtEndStep.add(tokenPermanent.getId());
        }

        String logMsg = effect.grantHaste()
                ? "A token copy of " + imprintedCard.getName() + " is created with haste."
                : "A token copy of " + imprintedCard.getName() + " is created.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - Token copy of {} created via {}", gameData.id, imprintedCard.getName(), sourcePermanent.getCard().getName());

        gameHelper.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, tokenPermanent.getId(), false);
    }

    @HandlesEffect(CreateTokenCopyOfSourceEffect.class)
    private void resolveCreateTokenCopyOfSource(GameData gameData, StackEntry entry) {
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourcePermanent == null) {
            log.info("Game {} - Source permanent no longer on battlefield", gameData.id);
            return;
        }

        Card sourceCard = sourcePermanent.getCard();

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
        gameHelper.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);

        String logMsg = "A token copy of " + sourceCard.getName() + " is created.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - Token copy of {} created via {}", gameData.id, sourceCard.getName(), sourceCard.getName());

        gameHelper.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, tokenPermanent.getId(), false);
    }

}


