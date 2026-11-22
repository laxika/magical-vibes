package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.j.JourneyOn;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "231")
@CardRegistration(set = "LCI", collectorNumber = "341")
public class KellanDaringTraveler extends Card {

    public KellanDaringTraveler() {
        var match = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE), new CardMaxManaValuePredicate(3)));
        setBackFaceCard(new JourneyOn());
        addCastingOption(new AdventureCast());
        addTrigger(Triggers.attacks(), new RevealTopCardOfLibraryEffect(Subjects.you()),
                new PutCardsIntoHandEffect(Subjects.revealedThisWay(match)),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, new CardNotPredicate(match)),
                        new MayEffect(new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay()),
                        "Put the revealed card into your graveyard?")));
    }

    @Override
    public String getBackFaceClassName() {
        return "JourneyOn";
    }
}
