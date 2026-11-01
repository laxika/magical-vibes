package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "45")
@CardRegistration(set = "DDQ", collectorNumber = "24")
public class CaptainOfTheMists extends Card {

    public CaptainOfTheMists() {
        // Whenever another Human you control enters, untap this creature.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.HUMAN),
                        new UntapPermanentsEffect(TapUntapScope.SELF)));

        // {1}{U}, {T}: You may tap or untap target permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new MayEffect(new TapOrUntapTargetPermanentEffect(), "Tap or untap target permanent?")),
                "{1}{U}, {T}: You may tap or untap target permanent.",
                TargetFilters.permanent()
        ));
    }
}
