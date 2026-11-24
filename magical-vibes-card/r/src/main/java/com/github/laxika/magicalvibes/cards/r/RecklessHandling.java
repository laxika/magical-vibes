package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MAT", collectorNumber = "19")
@CardRegistration(set = "MAT", collectorNumber = "69")
@CardRegistration(set = "MAT", collectorNumber = "119")
@CardRegistration(set = "MAT", collectorNumber = "203")
public class RecklessHandling extends Card {

    public RecklessHandling() {
        addSpellEffect(new SearchLibraryEffect(new CardTypePredicate(CardType.ARTIFACT)));
        addSpellEffect(new DiscardEffect(1, Subjects.you(), true));
        addSpellEffect(new ConditionalEffect(
                HappenedThisWay.toACard(StepKind.DISCARD, new CardTypePredicate(CardType.ARTIFACT)),
                new DealDamageToPlayersEffect(2, Subjects.eachOpponent())));
    }
}
