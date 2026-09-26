package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "39")
@CardRegistration(set = "LTC", collectorNumber = "122")
public class HaldirLRienLieutenant extends Card {

    public HaldirLRienLieutenant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        PermanentPredicate otherElf = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.ELF),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        CountersOnSource plusOneCounters = new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}",
                List.of(
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES, otherElf),
                        new BoostAllOwnCreaturesEffect(plusOneCounters, plusOneCounters, otherElf)
                ),
                "{5}{G}: Until end of turn, other Elves you control gain vigilance and get +1/+1 for each "
                        + "+1/+1 counter on Haldir."
        ));
    }
}
