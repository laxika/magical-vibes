package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyCardsExiledWithSourceAndMayCastCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "432")
public class ZethiArcaneBlademaster extends Card {

    public ZethiArcaneBlademaster() {
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.multikicker(List.of("{W/U}")));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                ExileCardsFromGraveyardEffect.upToMultikickerCards(
                        new CardTypePredicate(CardType.INSTANT)));
        addEffect(EffectSlot.ON_ATTACK,
                CopyCardsExiledWithSourceAndMayCastCopiesEffect.allKickCounterCardsForNormalCost());
    }
}
