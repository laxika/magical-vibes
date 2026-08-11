package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingTokenCreationReplacement;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MirrormindCrownEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToBattlefieldUnderOwnerControlEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final CreateTokenCopyOfEquippedCreatureEffectHandler tokenCopyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenEffect) effect;
        // Source-relative amounts use the live source permanent when it is still on the
        // battlefield, else the last-known snapshot (e.g. sacrificed as an activation cost).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int amount = amountEvaluationService.evaluate(gameData, e.amount(), context);
        if (amount <= 0) {
            return;
        }
        int power = amountEvaluationService.evaluate(gameData, e.power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, e.toughness(), context);

        PendingTokenCreationReplacement pending = gameData.pendingTokenCreationReplacement;
        if (pending != null) {
            gameData.pendingTokenCreationReplacement = null;
            Boolean accepted = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            if (Boolean.TRUE.equals(accepted)) {
                tokenCopyHandler.resolve(gameData, entry,
                        new CreateTokenCopyOfEquippedCreatureEffect(
                                pending.amount(), false, false, pending.crownPermanentId()));
                return;
            }
            if (Boolean.FALSE.equals(accepted)) {
                amount = pending.amount();
                power = pending.power();
                toughness = pending.toughness();
            }
        } else {
            Permanent crown = availableMirrormindCrown(gameData, entry.getControllerId());
            if (crown != null) {
                gameData.tokenCreationReplacementUsedThisTurn.add(crown.getId());
                gameData.pendingTokenCreationReplacement = new PendingTokenCreationReplacement(
                        crown.getId(), amount, power, toughness);
                gameData.resolvingMayEffectFromStack = true;
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        crown.getCard(),
                        entry.getControllerId(),
                        List.of(new CreateTokenCopyOfEquippedCreatureEffect(
                                amount, false, false, crown.getId())),
                        crown.getCard().getName()
                                + " — You may create that many tokens that are copies of the equipped creature."));
                return;
            }
        }

        entry.getCreatedPermanentIds().addAll(
                permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(), bindDeathReturn(e, entry), amount,
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
