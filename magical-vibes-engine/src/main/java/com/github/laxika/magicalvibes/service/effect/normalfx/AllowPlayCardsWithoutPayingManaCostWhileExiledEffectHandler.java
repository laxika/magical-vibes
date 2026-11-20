package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsWithoutPayingManaCostWhileExiledEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AllowPlayCardsWithoutPayingManaCostWhileExiledEffect}: each card its subject names in exile may be
 * played by the effect's controller without paying its mana cost for as long as it remains exiled. It never prompts.
 */
@Component
@RequiredArgsConstructor
public class AllowPlayCardsWithoutPayingManaCostWhileExiledEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final ExileSupport exileSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowPlayCardsWithoutPayingManaCostWhileExiledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var permission = (AllowPlayCardsWithoutPayingManaCostWhileExiledEffect) effect;
        for (SubjectResolutionService.LocatedCard located
                : subjectResolutionService.locatedCards(gameData, entry, permission, permission.cards())) {
            if (located.zone() != Zone.EXILE) {
                continue;
            }
            exileSupport.grantPlayWhileExiled(gameData, located.card().getId(), entry.getControllerId());
            gameData.setExilePlayWithoutPayingManaCost(located.card().getId(), true);
        }
    }
}
