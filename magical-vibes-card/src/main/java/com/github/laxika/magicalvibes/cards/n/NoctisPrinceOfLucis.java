package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "235")
public class NoctisPrinceOfLucis extends Card {

    public NoctisPrinceOfLucis() {
        addEffect(EffectSlot.STATIC, new CastSpellsFromGraveyardEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                List.of(new LifeCastingCost(3)), CounterType.FINALITY));
    }
}
