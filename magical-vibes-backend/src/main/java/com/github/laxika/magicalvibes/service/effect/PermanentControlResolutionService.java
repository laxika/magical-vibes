package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenWithColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentControlResolutionService implements EffectHandlerProvider {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(CreateCreatureTokenEffect.class,
                (gd, entry, effect) -> resolveCreateCreatureToken(gd, entry.getControllerId(), (CreateCreatureTokenEffect) effect));
        registry.register(CreateCreatureTokenWithColorsEffect.class,
                (gd, entry, effect) -> resolveCreateCreatureTokenWithColors(gd, entry.getControllerId(), (CreateCreatureTokenWithColorsEffect) effect));
        registry.register(PutAuraFromHandOntoSelfEffect.class,
                (gd, entry, effect) -> resolvePutAuraFromHandOntoSelf(gd, entry));
        registry.register(PutTargetOnBottomOfLibraryEffect.class,
                (gd, entry, effect) -> resolvePutTargetOnBottomOfLibrary(gd, entry));
        registry.register(SacrificeAtEndOfCombatEffect.class,
                (gd, entry, effect) -> resolveSacrificeAtEndOfCombat(gd, entry));
        registry.register(SacrificeSelfEffect.class,
                (gd, entry, effect) -> resolveSacrificeSelf(gd, entry));
        registry.register(RedirectUnblockedCombatDamageToSelfEffect.class,
                (gd, entry, effect) -> resolveRedirectUnblockedCombatDamageToSelf(gd, entry));
        registry.register(RegenerateEffect.class,
                (gd, entry, effect) -> resolveRegenerate(gd, entry));
        registry.register(GainControlOfTargetAuraEffect.class,
                (gd, entry, effect) -> resolveGainControlOfTargetAura(gd, entry));
        registry.register(GainControlOfEnchantedTargetEffect.class,
                (gd, entry, effect) -> resolveGainControlOfEnchantedTarget(gd, entry));
        registry.register(GainControlOfTargetCreatureUntilEndOfTurnEffect.class,
                (gd, entry, effect) -> resolveGainControlOfTargetCreatureUntilEndOfTurn(gd, entry));
    }

    private void resolveCreateCreatureToken(GameData gameData, UUID controllerId, CreateCreatureTokenEffect token) {
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
            gameData.playerBattlefields.get(controllerId).add(tokenPermanent);

            String logEntry = "A " + token.power() + "/" + token.toughness() + " " + token.tokenName() + " creature token enters the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null);
            if (gameData.interaction.awaitingInput == null) {
                gameHelper.checkLegendRule(gameData, controllerId);
            }
        }

        log.info("Game {} - {} {} token(s) created for player {}", gameData.id, token.amount(), token.tokenName(), controllerId);
    }

    private void resolveCreateCreatureTokenWithColors(GameData gameData, UUID controllerId, CreateCreatureTokenWithColorsEffect token) {
        Card tokenCard = new Card();
        tokenCard.setName(token.tokenName());
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(token.primaryColor());
        tokenCard.setPower(token.power());
        tokenCard.setToughness(token.toughness());
        tokenCard.setSubtypes(token.subtypes());

        Permanent tokenPermanent = new Permanent(tokenCard);
        gameData.playerBattlefields.get(controllerId).add(tokenPermanent);

        String colorNames = token.colors().stream()
                .map(c -> c.name().charAt(0) + c.name().substring(1).toLowerCase())
                .reduce((a, b) -> a + " and " + b).orElse("");
        String logEntry = "A " + token.power() + "/" + token.toughness() + " " + colorNames + " " + token.tokenName() + " creature token enters the battlefield.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null);
        if (gameData.interaction.awaitingInput == null) {
            gameHelper.checkLegendRule(gameData, controllerId);
        }

        log.info("Game {} - {} token created for player {}", gameData.id, token.tokenName(), controllerId);
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

    private void resolveSacrificeSelf(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        if (gameHelper.removePermanentToGraveyard(gameData, self)) {
            String logEntry = self.getCard().getName() + " is sacrificed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            gameHelper.removeOrphanedAuras(gameData);
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
            gameData.interaction.permanentChoiceContext = new PermanentChoiceContext.AuraGraft(aura.getId());
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

    private void resolveGainControlOfTargetCreatureUntilEndOfTurn(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        UUID oldController = findControllerId(gameData, target);
        if (oldController == null || oldController.equals(entry.getControllerId())) {
            return;
        }

        gameHelper.stealCreature(gameData, entry.getControllerId(), target);
        gameData.untilEndOfTurnStolenCreatures.add(target.getId());
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

}

