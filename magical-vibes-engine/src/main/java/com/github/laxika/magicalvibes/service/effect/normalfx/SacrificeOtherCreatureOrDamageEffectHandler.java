package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SacrificeOtherCreatureOrDamageEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final DamageSupport damageSupport;
    private final PlayerInputService playerInputService;

    @Autowired
    public SacrificeOtherCreatureOrDamageEffectHandler(DestructionSupport destructionSupport,
            GameOutcomeService gameOutcomeService, GameQueryService gameQueryService,
            AmountEvaluationService amountEvaluationService, DamageSupport damageSupport,
            PlayerInputService playerInputService) {
        this.destructionSupport = destructionSupport;
        this.gameOutcomeService = gameOutcomeService;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
        this.damageSupport = damageSupport;
        this.playerInputService = playerInputService;
    }

    public SacrificeOtherCreatureOrDamageEffectHandler(DestructionSupport destructionSupport,
            GameOutcomeService gameOutcomeService, GameQueryService gameQueryService,
            PlayerInputService playerInputService) {
        this.destructionSupport = destructionSupport;
        this.gameOutcomeService = gameOutcomeService;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = null;
        this.damageSupport = null;
        this.playerInputService = playerInputService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOtherCreatureOrDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeOtherCreatureOrDamageEffect) effect;
        UUID controllerId = sacrificingPlayer(entry, e.recipient());
        if (controllerId == null) {
            return;
        }
        String cardName = entry.getCard().getName();
        UUID excludedPermanentId = excludedPermanentId(gameData, entry, e.recipient());

        List<UUID> otherCreatureIds = destructionSupport.collectCreatureIds(gameData, controllerId,
                p -> excludedPermanentId == null || !p.getId().equals(excludedPermanentId));

        if (otherCreatureIds.isEmpty()) {
            dealDamage(gameData, entry, e.damage(), controllerId);
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        if (otherCreatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, otherCreatureIds.getFirst());
            if (creature != null) {
                destructionSupport.sacrificeAndLog(gameData, creature, controllerId);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificeCreature(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, otherCreatureIds,
                "Choose a creature other than " + cardName + " to sacrifice.");
    }

    private UUID sacrificingPlayer(StackEntry entry, DamageRecipient recipient) {
        return recipient == DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER
                ? entry.getTargetId()
                : entry.getControllerId();
    }

    private UUID excludedPermanentId(GameData gameData, StackEntry entry, DamageRecipient recipient) {
        if (recipient != DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER) {
            return entry.getSourcePermanentId();
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        return source == null ? null : source.getAttachedTo();
    }

    private void dealDamage(GameData gameData, StackEntry entry, DynamicAmount amount, UUID playerId) {
        if (amount instanceof Fixed fixed) {
            destructionSupport.dealNoncombatDamageToPlayer(gameData, playerId, fixed.value(),
                    entry.getCard().getName(), entry.getEffectiveDamageSourceCard());
            return;
        }
        if (damageSupport == null) {
            throw new IllegalStateException("Dynamic sacrifice damage requires the engine handler constructor");
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int evaluatedDamage = amountEvaluationService.evaluate(gameData, amount, context);
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, evaluatedDamage, entry);
        damageSupport.dealDamageToPlayer(gameData, entry, playerId, rawDamage);
    }
}
