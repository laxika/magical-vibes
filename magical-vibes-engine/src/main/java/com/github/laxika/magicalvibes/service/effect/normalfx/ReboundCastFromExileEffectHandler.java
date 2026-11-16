package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReboundCastFromExileEffect;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReboundCastFromExileEffectHandler implements NormalEffectHandlerBean {

    /**
     * The delayed triggered ability "at the beginning of your next upkeep, you may cast {@code card} from exile without
     * paying its mana cost" that the spell {@code card} controlled by {@code controllerId} made as it resolved
     * (CR 702.88a, 603.7d), about that card in exile.
     */
    public static TemporaryGlobalTriggeredAbility reboundAbility(UUID controllerId, Card card,
                                                                 boolean exileInsteadOfGraveyard) {
        return TemporaryGlobalTriggeredAbility.atNextStep(controllerId, card,
                new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                        new ReboundCastFromExileEffect(exileInsteadOfGraveyard)),
                null, null, null, DelayedReferents.cards(List.of(card.getId())));
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReboundCastFromExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.findExiledCard(entry.getCard().getId()) == null) {
            return;
        }
        if (((ReboundCastFromExileEffect) effect).exileInsteadOfGraveyard()) {
            gameData.exileInsteadOfGraveyard.add(entry.getCard().getId());
        }
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                entry.getCard().getName() + " - You may cast this card from exile without paying its mana cost.",
                entry.getCard().getId()
        ));
    }
}
