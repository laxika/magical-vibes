package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "103")
public class GisaGloriousResurrector extends Card {

    public GisaGloriousResurrector() {
        addEffect(EffectSlot.STATIC,
                ExileOpponentCreaturesInsteadOfDyingEffect.withSourceTracking());
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ReturnAllCardsExiledWithSourceEffect(true,
                        new CardTypePredicate(CardType.CREATURE), false, Set.of(Keyword.DECAYED)));
    }
}
