package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "MIC", collectorNumber = "19")
@CardRegistration(set = "MIC", collectorNumber = "57")
public class GhoulsNightOut extends Card {

    public GhoulsNightOut() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesCardFromGraveyardToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE), CardColor.BLACK, CardSubtype.ZOMBIE,
                Set.of(Keyword.DECAYED)));
    }
}
