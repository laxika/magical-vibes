package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileCastTargetSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetCardFromGraveyardAndMayCastCopyHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final LifeSupport lifeSupport;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;
    private final SpellCastingService spellCastingService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCardFromGraveyardAndMayCastCopyEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card copy = ability.sourceCard();
        ExileTargetCardFromGraveyardAndMayCastCopyEffect effect = ability.effects().stream()
                .filter(ExileTargetCardFromGraveyardAndMayCastCopyEffect.class::isInstance)
                .map(ExileTargetCardFromGraveyardAndMayCastCopyEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (!accepted) {
            gameData.removeFromExile(copy.getId());
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " declines to cast the copy of ", copy, "."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        ExiledCardEntry exiledEntry = gameData.findExiledCard(copy.getId());
        if (exiledEntry == null || copy.hasType(CardType.LAND)) {
            gameData.removeFromExile(copy.getId());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (effect.withoutPayingManaCost()) {
            gameData.exilePlayWithoutPayingManaCost.add(copy.getId());
        }

        StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(copy);
        List<CardEffect> spellEffects = new ArrayList<>(copy.getEffects(EffectSlot.SPELL));
        if (EffectResolution.needsTarget(copy)) {
            List<UUID> firstCandidates = exileCastTargetSupport.firstSlotCandidates(
                    gameData, copy, player.getId());
            boolean hasLegalTargets = copy.getMaxTargets() > 1
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, copy, player.getId())
                    : !firstCandidates.isEmpty();
            if (!hasLegalTargets) {
                gameData.removeFromExile(copy.getId());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    PermanentChoiceContext.ExileCastSpellTarget.resolutionCastCopy(
                            copy, player.getId(), spellEffects, spellType, effect.lifeLossOnCast()));
            playerInputService.beginPermanentChoice(gameData, player.getId(), firstCandidates,
                    "Choose a target for " + copy.getName() + ".");
            return;
        }

        try {
            spellCastingService.playCardFromExileAsResolutionCast(
                    gameData, player, copy.getId(), 0, (UUID) null, true);
            applyLifeLoss(gameData, player, copy, effect.lifeLossOnCast());
        } catch (IllegalStateException ex) {
            gameData.removeFromExile(copy.getId());
            gameLogService.append(gameData, GameLog.cardThen(copy,
                    " can't be cast and ceases to exist."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void applyLifeLoss(GameData gameData, Player player, Card copy, int amount) {
        if (amount > 0) {
            lifeSupport.applyLifeLoss(gameData, player.getId(), amount, copy.getName());
        }
    }
}
