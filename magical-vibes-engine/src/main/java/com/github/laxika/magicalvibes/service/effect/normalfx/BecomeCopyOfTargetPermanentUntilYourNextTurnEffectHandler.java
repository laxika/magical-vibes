package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetPermanentUntilYourNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/** Resolves temporary copy effects with fixed copy exceptions that last until the controller's next turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfTargetPermanentUntilYourNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfTargetPermanentUntilYourNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        UUID targetId = entry.getTargetId();
        if (sourceId == null || targetId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (source == null || target == null) {
            log.info("Game {} - Copy-until-next-turn source or target no longer exists", gameData.id);
            return;
        }

        BecomeCopyOfTargetPermanentUntilYourNextTurnEffect copyEffect =
                (BecomeCopyOfTargetPermanentUntilYourNextTurnEffect) effect;
        if (!source.isCopyUntilControllerNextTurn()) {
            source.setUntilNextTurnPreCopyCard(source.getCard());
        }

        String originalName = source.getCard().getName();
        String targetName = target.getCard().getName();
        permanentCopierService.applyCloneCopy(source, target,
                copyEffect.powerOverride(), copyEffect.toughnessOverride(),
                copyEffect.additionalTypesOverride());

        if (copyEffect.nameOverride() != null) {
            source.getCard().setName(copyEffect.nameOverride());
        }
        if (!copyEffect.additionalSubtypesOverride().isEmpty()) {
            List<CardSubtype> subtypes = new ArrayList<>(source.getCard().getSubtypes());
            for (CardSubtype subtype : copyEffect.additionalSubtypesOverride()) {
                if (!subtypes.contains(subtype)) {
                    subtypes.add(subtype);
                }
            }
            source.getCard().setSubtypes(List.copyOf(subtypes));
        }
        if (!copyEffect.additionalSupertypesOverride().isEmpty()) {
            EnumSet<CardSupertype> supertypes = EnumSet.noneOf(CardSupertype.class);
            supertypes.addAll(source.getCard().getSupertypes());
            supertypes.addAll(copyEffect.additionalSupertypesOverride());
            source.getCard().setSupertypes(supertypes);
        }
        if (!copyEffect.additionalKeywordsOverride().isEmpty()) {
            EnumSet<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            keywords.addAll(source.getCard().getKeywords());
            keywords.addAll(copyEffect.additionalKeywordsOverride());
            source.getCard().setKeywords(keywords);
        }

        source.setCopyUntilControllerNextTurn(true);
        source.setCopyUntilNextTurnControllerId(entry.getControllerId());
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), sourceId,
                entry.getControllerId(), new MakeTargetCopyOfTargetCreatureUntilNextTurnEffect(),
                sourceId, null, null, EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));

        gameLogService.append(gameData,
                GameLog.text(originalName + " becomes a copy of " + targetName
                        + " until your next turn."));
        log.info("Game {} - {} becomes a copy of {} until the controller's next turn",
                gameData.id, originalName, targetName);
    }
}
