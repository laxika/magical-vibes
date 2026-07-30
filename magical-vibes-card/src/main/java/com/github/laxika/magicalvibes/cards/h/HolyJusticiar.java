package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Holy Justiciar — {3}{W} Creature — Human Cleric 2/1.
 * {2}{W}, {T}: Tap target creature. If that creature is a Zombie, exile it.
 */
@CardRegistration(set = "AVR", collectorNumber = "25")
public class HolyJusticiar extends Card {

    public HolyJusticiar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new ConditionalEffect(
                                new TargetPermanentMatches(
                                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)),
                                new ExileTargetPermanentEffect())
                ),
                "{2}{W}, {T}: Tap target creature. If that creature is a Zombie, exile it.",
                TargetFilters.creature()
        ));
    }
}
