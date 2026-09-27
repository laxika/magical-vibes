package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExilePlayCostModifier;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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

        var permission = (AllowPlayCardsExiledWithSourceUntilEndOfTurnEffect) effect;
        List<ExiledCardEntry> matchingEntries = gameData.getExiledWithPermanentEntries(
                sourcePermanentId, entry.getCard().getId());
        for (ExiledCardEntry matchingEntry : matchingEntries) {
            var card = matchingEntry.card();
            gameData.exilePlayPermissions.put(card.getId(), entry.getControllerId());
            gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
            if (!card.hasType(CardType.LAND) && permission.spellCostReduction() > 0) {
                gameData.exilePlayCostModifiers.put(card.getId(),
                        new ExilePlayCostModifier(entry.getControllerId(), null,
                                -permission.spellCostReduction()));
            }
        }
    }
}
