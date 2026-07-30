package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "45")
public class CaptainOfTheMists extends Card {

    public CaptainOfTheMists() {
        // Whenever another Human you control enters, untap this creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.HUMAN),
                        new UntapPermanentsEffect(TapUntapScope.SELF)));

        // {1}{U}, {T}: You may tap or untap target permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new TapOrUntapTargetPermanentEffect()),
                "{1}{U}, {T}: You may tap or untap target permanent.",
                TargetFilters.permanent()
        ));
    }
}
