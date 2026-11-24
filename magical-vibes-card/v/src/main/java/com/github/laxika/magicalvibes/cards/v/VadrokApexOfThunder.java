package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "214")
public class VadrokApexOfThunder extends Card {

    public VadrokApexOfThunder() {
        addTrigger(Triggers.mutates(), CastCardFromGraveyardEffect.of(Subjects.targetCard(
                new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        new CardMaxManaValuePredicate(3))),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD)).withoutPayingItsManaCost());
    }
}
