package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SacrificeOtherCreatureOrDamageEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

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
                p -> (excludedPermanentId == null || !p.getId().equals(excludedPermanentId))
                        && !gameQueryService.cantBeChosenToSacrifice(gameData, p));

        if (otherCreatureIds.isEmpty()) {
            dealDamage(gameData, entry, e.damage(), controllerId);
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        if (e.recipient() == DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), controllerId, List.of(e),
                    "Sacrifice another creature instead of " + cardName + " dealing damage to you?",
                    null, null, entry.getSourcePermanentId()));
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

    /**
     * Resolves the accept/decline answer to "sacrifice another creature instead?". Accepting sacrifices
     * a creature other than the enchanted one (asking when several are eligible); declining, or having
     * nothing left to sacrifice, deals the damage to the enchanted creature's controller.
     */
    public void resolveMayChoice(GameData gameData, boolean accepted, PendingMayAbility ability) {
        var e = (SacrificeOtherCreatureOrDamageEffect) ability.effects().getFirst();
        UUID controllerId = ability.controllerId();
        if (accepted) {
            Permanent source = gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
            UUID excludedPermanentId = source == null ? null : source.getAttachedTo();
            List<UUID> otherCreatureIds = destructionSupport.collectCreatureIds(gameData, controllerId,
                    p -> (excludedPermanentId == null || !p.getId().equals(excludedPermanentId))
                            && !gameQueryService.cantBeChosenToSacrifice(gameData, p));
            if (otherCreatureIds.size() == 1) {
                Permanent creature = gameQueryService.findPermanentById(gameData, otherCreatureIds.getFirst());
                if (creature != null) {
                    destructionSupport.sacrificeAndLog(gameData, creature, controllerId);
                }
                return;
            }
            if (otherCreatureIds.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificeCreature(controllerId));
                playerInputService.beginPermanentChoice(gameData, controllerId, otherCreatureIds,
                        "Choose a creature other than " + ability.sourceCard().getName() + " to sacrifice.");
                return;
            }
        }
        StackEntry damageEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(),
                controllerId, ability.sourceCard().getName() + "'s ability", new ArrayList<>(List.of(e)),
                controllerId, ability.sourcePermanentId());
        dealDamage(gameData, damageEntry, e.damage(), controllerId);
        gameOutcomeService.checkWinCondition(gameData);
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
            destructionSupport.dealNoncombatDamageToPlayer(gameData, entry, playerId, fixed.value());
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
        int rawDamage = evaluatedDamage;
        damageSupport.dealDamageToPlayer(gameData, entry, playerId, rawDamage);
    }
}
