package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "88")
@CardRegistration(set = "HOP", collectorNumber = "30")
public class Helldozer extends Card {

    public Helldozer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}{B}{B}",
                List.of(new DestroyTargetPermanentThenEffect(
                        EventStat.NONE,
                        new UntapPermanentsEffect(TapUntapScope.SOURCE_PERMANENT),
                        ThenEffectRecipient.CONTROLLER,
                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC)))),
                "{B}{B}{B}, {T}: Destroy target land. If that land was nonbasic, untap this creature.",
                TargetFilters.land()
        ));
    }
}
