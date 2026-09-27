package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilitiesOfLandCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TDC", collectorNumber = "48")
@CardRegistration(set = "TDC", collectorNumber = "88")
public class StewardOfTheHarvest extends Card {

    public StewardOfTheHarvest() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileCardsFromGraveyardEffect(3, new CardTypePredicate(CardType.LAND),
                        true, true, true));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilitiesOfLandCardsExiledWithSourceEffect());
    }
}
