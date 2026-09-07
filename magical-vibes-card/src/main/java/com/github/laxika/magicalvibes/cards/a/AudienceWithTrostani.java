package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DistinctPermanentNamesCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "152")
@CardRegistration(set = "MKM", collectorNumber = "309")
public class AudienceWithTrostani extends Card {

    public AudienceWithTrostani() {
        addEffect(EffectSlot.SPELL,
                new CreateTokenEffect("Plant", 0, 1, CardColor.GREEN, List.of(CardSubtype.PLANT), Set.of(), Set.of()));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new DistinctPermanentNamesCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTokenPredicate()
                )),
                CountScope.CONTROLLER)));
    }
}
