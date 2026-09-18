package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.CopyCardsExiledWithSourceAndMayCastCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

import java.util.List;

@CardRegistration(set = "SLX", collectorNumber = "17")
public class ZethiArcaneBlademaster extends Card {

    public ZethiArcaneBlademaster() {
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.multikicker(List.of("{W/U}")));

        CardTypePredicate instant = new CardTypePredicate(CardType.INSTANT);
        targetUpTo(new RepeatedAdditionalCostCount("{W/U}"),
                new GraveyardCardPredicateTargetFilter(instant, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 100)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        ExileGraveyardCardsEffect.upToControllerGraveyardWithKickCounters(instant));

        addEffect(EffectSlot.ON_ATTACK,
                CopyCardsExiledWithSourceAndMayCastCopiesEffect.allOwnedKickCounterCardsForNormalCost());
    }
}
