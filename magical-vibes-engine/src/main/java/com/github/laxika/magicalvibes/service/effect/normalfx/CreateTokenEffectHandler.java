package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingTokenCreationReplacement;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MirrormindCrownEffect;
import com.github.laxika.magicalvibes.model.effect.MoonlitMeditationEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToBattlefieldUnderOwnerControlEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class CreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final CreateTokenCopyOfEquippedCreatureEffectHandler tokenCopyHandler;
    private final CreateTokenCopyOfEnchantedPermanentEffectHandler enchantedPermanentTokenCopyHandler;
    private final TriggerCollectionService triggerCollectionService;

    public CreateTokenEffectHandler(PermanentControlSupport permanentControlSupport,
                                    GameQueryService gameQueryService,
                                    AmountEvaluationService amountEvaluationService,
                                    CreateTokenCopyOfEquippedCreatureEffectHandler tokenCopyHandler,
                                    CreateTokenCopyOfEnchantedPermanentEffectHandler enchantedPermanentTokenCopyHandler,
                                    @Lazy TriggerCollectionService triggerCollectionService) {
        this.permanentControlSupport = permanentControlSupport;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
        this.tokenCopyHandler = tokenCopyHandler;
        this.enchantedPermanentTokenCopyHandler = enchantedPermanentTokenCopyHandler;
        this.triggerCollectionService = triggerCollectionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        resolveForController(gameData, entry, (CreateTokenEffect) effect, entry.getControllerId());
    }

    public void resolveForController(GameData gameData, StackEntry entry, CreateTokenEffect e,
                                     UUID controllerId) {
        // Source-relative amounts use the live source permanent when it is still on the
        // battlefield, else the last-known snapshot (e.g. sacrificed as an activation cost).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source).withControllerId(controllerId);
        int amount = amountEvaluationService.evaluate(gameData, e.amount(), context);
        if (amount <= 0) {
            return;
        }
        if (e.subtypes().contains(CardSubtype.CLUE)) {
            triggerCollectionService.checkInvestigateTriggers(gameData, controllerId);
        }
        int power = amountEvaluationService.evaluate(gameData, e.power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, e.toughness(), context);

        PendingTokenCreationReplacement pending = gameData.pendingTokenCreationReplacement;
        if (pending != null) {
            gameData.pendingTokenCreationReplacement = null;
            Boolean accepted = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            if (Boolean.TRUE.equals(accepted)) {
                if (pending.copyEnchantedPermanent()) {
                    enchantedPermanentTokenCopyHandler.resolve(gameData, entry,
                            new CreateTokenCopyOfEnchantedPermanentEffect(
                                    pending.amount(), pending.replacementPermanentId()));
                } else {
                    tokenCopyHandler.resolve(gameData, entry,
                            new CreateTokenCopyOfEquippedCreatureEffect(
                                    pending.amount(), false, false, pending.replacementPermanentId()));
                }
                return;
            }
            if (Boolean.FALSE.equals(accepted)) {
                amount = pending.amount();
                power = pending.power();
                toughness = pending.toughness();
            }
        } else {
            Permanent crown = availableMirrormindCrown(gameData, controllerId);
            if (crown != null) {
                gameData.tokenCreationReplacementUsedThisTurn.add(crown.getId());
                gameData.pendingTokenCreationReplacement = new PendingTokenCreationReplacement(
                        crown.getId(), amount, power, toughness, false);
                gameData.resolvingMayEffectFromStack = true;
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        crown.getCard(),
                        controllerId,
                        List.of(new CreateTokenCopyOfEquippedCreatureEffect(
                                amount, false, false, crown.getId())),
                        crown.getCard().getName()
                                + " — You may create that many tokens that are copies of the equipped creature."));
                return;
            }
            Permanent moonlit = availableMoonlitMeditation(gameData, controllerId);
            if (moonlit != null) {
                gameData.tokenCreationReplacementUsedThisTurn.add(moonlit.getId());
                gameData.pendingTokenCreationReplacement = new PendingTokenCreationReplacement(
                        moonlit.getId(), amount, power, toughness, true);
                gameData.resolvingMayEffectFromStack = true;
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        moonlit.getCard(),
                        controllerId,
                        List.of(new CreateTokenCopyOfEnchantedPermanentEffect(amount, moonlit.getId())),
                        moonlit.getCard().getName()
                                + " — You may create that many tokens that are copies of the enchanted permanent."));
                return;
            }
        }

        entry.getCreatedPermanentIds().addAll(
                permanentControlSupport.applyCreateToken(gameData, controllerId, bindDeathReturn(e, entry), amount,
                        entry.getCard().getSetCode(), power, toughness));
    }

    private Permanent availableMirrormindCrown(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return null;
        }
        for (Permanent permanent : battlefield) {
            if (gameData.tokenCreationReplacementUsedThisTurn.contains(permanent.getId())
                    || permanent.getAttachedTo() == null
                    || permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .noneMatch(MirrormindCrownEffect.class::isInstance)) {
                continue;
            }
            if (gameQueryService.findPermanentById(gameData, permanent.getAttachedTo()) != null) {
                return permanent;
            }
        }
        return null;
    }

    private Permanent availableMoonlitMeditation(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return null;
        }
        for (Permanent permanent : battlefield) {
            if (gameData.tokenCreationReplacementUsedThisTurn.contains(permanent.getId())
                    || permanent.getAttachedTo() == null
                    || permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .noneMatch(MoonlitMeditationEffect.class::isInstance)) {
                continue;
            }
            if (gameQueryService.findPermanentById(gameData, permanent.getAttachedTo()) != null) {
                return permanent;
            }
        }
        return null;
    }

    /**
     * Binds an authored "return this card from exile when the token dies" death trigger to the card
     * that created the token (Tatsumasa, the Dragon's Fang, exiled to pay its own activation cost).
     * The id is only knowable at resolution, so the blueprint carries a {@code null} placeholder.
     */
    private static CreateTokenEffect bindDeathReturn(CreateTokenEffect token, StackEntry entry) {
        Map<EffectSlot, CardEffect> tokenEffects = token.tokenEffects();
        if (tokenEffects == null || entry.getCard() == null) {
            return token;
        }
        CardEffect deathEffect = tokenEffects.get(EffectSlot.ON_DEATH);
        if (!(deathEffect instanceof ReturnExiledCardToBattlefieldUnderOwnerControlEffect returnEffect)
                || returnEffect.exiledCardId() != null) {
            return token;
        }
        Map<EffectSlot, CardEffect> bound = new EnumMap<>(tokenEffects);
        bound.put(EffectSlot.ON_DEATH,
                new ReturnExiledCardToBattlefieldUnderOwnerControlEffect(entry.getCard().getId()));
        return token.withTokenEffects(bound);
    }
}
