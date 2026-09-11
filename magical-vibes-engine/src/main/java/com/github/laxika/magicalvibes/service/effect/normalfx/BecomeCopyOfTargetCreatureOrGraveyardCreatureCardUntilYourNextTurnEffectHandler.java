package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureOrGraveyardCreatureCardUntilYourNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/** Resolves Taskmaster's copy effect for either a battlefield creature or a graveyard creature card. */
@Component
@RequiredArgsConstructor
public class BecomeCopyOfTargetCreatureOrGraveyardCreatureCardUntilYourNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfTargetCreatureOrGraveyardCreatureCardUntilYourNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        Permanent source = sourceId == null ? null : gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        Card targetCard;
        Permanent targetPermanent = null;
        if (entry.getTargetZone() == Zone.GRAVEYARD) {
            UUID targetCardId = entry.getTargetCardIds().isEmpty()
                    ? entry.getTargetId() : entry.getTargetCardIds().getFirst();
            targetCard = targetCardId == null
                    ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
            if (targetCard == null || !targetCard.hasType(CardType.CREATURE)) {
                return;
            }
        } else {
            targetPermanent = entry.getTargetId() == null
                    ? null : gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (targetPermanent == null || !gameQueryService.isCreature(gameData, targetPermanent)) {
                return;
            }
            targetCard = targetPermanent.getCard();
        }

        BecomeCopyOfTargetCreatureOrGraveyardCreatureCardUntilYourNextTurnEffect copyEffect =
                (BecomeCopyOfTargetCreatureOrGraveyardCreatureCardUntilYourNextTurnEffect) effect;
        if (!source.isCopyUntilControllerNextTurn()) {
            source.setUntilNextTurnPreCopyCard(source.getCard());
        }

        String originalName = source.getCard().getName();
        String targetName = targetCard.getName();
        permanentCopierService.applyCloneCopy(source, targetCard, null, null,
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
    }
}
