package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExilePlayCostModifier;
import com.github.laxika.magicalvibes.model.ExilePlayGrantBySource;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExilePlayPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AllowPlayCardsExiledWithSourceUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowPlayCardsExiledWithSourceUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        var grant = (AllowPlayCardsExiledWithSourceUntilEndOfTurnEffect) effect;
        ExilePlayCostModifier reduction = grant.spellCostReduction() > 0
                ? new ExilePlayCostModifier(entry.getControllerId(), null, -grant.spellCostReduction()) : null;
        gameData.grantExilePlayUntilEndOfTurnBySource(sourcePermanentId, new ExilePlayGrantBySource(
                Subjects.exiledWithSource(), new ExilePlayPermissionEffect(entry.getControllerId())
                .withCostModifier(reduction)));
        List<ExiledCardEntry> matchingEntries = gameData.getExiledWithPermanentEntries(
                sourcePermanentId, entry.getCard().getId());
        for (ExiledCardEntry matchingEntry : matchingEntries) {
            var card = matchingEntry.card();
            gameData.grantExilePlayPermission(card.getId(), entry.getControllerId(), Duration.UNTIL_END_OF_TURN);
            if (!card.hasType(CardType.LAND) && grant.spellCostReduction() > 0) {
                gameData.updateExilePlayPermission(card.getId(), granted -> granted.withCostModifier(
                        new ExilePlayCostModifier(entry.getControllerId(), null, -grant.spellCostReduction())));
            }
        }
    }
}
