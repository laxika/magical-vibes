package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExilePlayPermissionEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link AllowPlayCardsFromExileEffect}: each card its subject names in exile may be played by the effect's
 * controller (or by the card's owner) for the effect's duration, with its riders (free, any type of mana, exile instead of a graveyard, a
 * condition that must hold as the card is played). A permission for as long as the controller controls the source is
 * granted only while they control it, linked to that source. It never prompts.
 */
@Component
@RequiredArgsConstructor
public class AllowPlayCardsFromExileEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowPlayCardsFromExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var permission = (AllowPlayCardsFromExileEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        boolean whileSourceControlled = Duration.WHILE_SOURCE_CONTROLLED.equals(permission.duration());
        if (whileSourceControlled) {
            Permanent source = sourcePermanentId == null
                    ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
            if (source == null || !controllerId.equals(gameQueryService.findPermanentController(gameData,
                    sourcePermanentId))) {
                return;
            }
        }
        for (SubjectResolutionService.LocatedCard located
                : subjectResolutionService.locatedCards(gameData, entry, permission, permission.cards())) {
            if (located.zone() != Zone.EXILE) {
                continue;
            }
            UUID cardId = located.card().getId();
            UUID holderId = permission.ownerPlays() && located.ownerId() != null ? located.ownerId() : controllerId;
            if (whileSourceControlled) {
                gameData.grantExilePlayPermission(cardId, new ExilePlayPermissionEffect(controllerId),
                        sourcePermanentId, controllerId, Duration.WHILE_SOURCE_CONTROLLED);
            } else {
                gameData.grantExilePlayPermission(cardId, holderId, permission.duration());
            }
            if (permission.freeCast()) {
                gameData.setExilePlayWithoutPayingManaCost(cardId, true);
            } else if (permission.anyManaType() && !located.card().hasType(CardType.LAND)) {
                gameData.grantExileAnyManaType(cardId, holderId, permission.duration());
            }
            if (permission.exileInsteadOfGraveyard()) {
                gameData.exileInsteadOfGraveyard(entry, cardId);
            }
            if (permission.playCondition() != null) {
                gameData.updateExilePlayPermission(cardId,
                        granted -> granted.withCondition(permission.playCondition()));
            }
        }
    }
}
