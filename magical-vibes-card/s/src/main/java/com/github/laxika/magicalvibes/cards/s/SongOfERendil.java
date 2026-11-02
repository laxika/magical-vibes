package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "69")
@CardRegistration(set = "LTC", collectorNumber = "479")
public class SongOfERendil extends Card {

    public SongOfERendil() {
        addTrigger(Triggers.chapter(1), new ScryEffect(2));
        addTrigger(Triggers.chapter(1), new DrawCardEffect(2));

        addTrigger(Triggers.chapter(2), CreateTokenEffect.ofTreasureToken(1).withSimultaneousTokens(List.of(
                new CreateTokenEffect(
                        1, "Bird", 2, 2, CardColor.BLUE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()))));

        addTrigger(Triggers.chapter(3), new PutCounterOnEachControlledPermanentEffect(
                CounterType.FLYING, 1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                ))));
    }
}
