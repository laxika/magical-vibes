package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.CopyExceptions;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "184")
@CardRegistration(set = "RVR", collectorNumber = "196")
@CardRegistration(set = "MKC", collectorNumber = "214")
public class LazavTheMultifarious extends Card {

    public LazavTheMultifarious() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(BecomeCopyOfSubjectEffect.of(Subjects.targetCard(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE), new CardManaValueEqualsXPredicate())),
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                        .keepingThisAbility()
                        .except(CopyExceptions.named("Lazav, the Multifarious").alsoLegendary())),
                "{X}: Lazav becomes a copy of target creature card in your graveyard with mana value X, "
                        + "except its name is Lazav, the Multifarious, it's legendary in addition to its other "
                        + "types, and it has this ability."
        ));
    }
}
