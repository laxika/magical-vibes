package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "75")
@CardRegistration(set = "AKR", collectorNumber = "89")
@CardRegistration(set = "CMM", collectorNumber = "131")
@CardRegistration(set = "C20", collectorNumber = "126")
public class VizierOfTumblingSands extends Card {

    public VizierOfTumblingSands() {
        // {T}: Untap another target permanent. "Another" excludes the source itself; targeting is
        // narrowed to any non-source permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{T}: Untap another target permanent.",
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        "Target must be another permanent"
                )
        ));

        // Cycling {1}{U} ({1}{U}, Discard this card: Draw a card.) with "When you cycle this card,
        // untap target permanent." The untap target is chosen at activation and is optional (0 to 1),
        // so cycling stays available with no legal target; the reflexive trigger then never goes on the
        // stack, and the trigger resolves above the cycling draw.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{U}",
                List.of(new DrawCardEffect(1)),
                "Cycling {1}{U} ({1}{U}, Discard this card: Draw a card.)",
                TargetFilters.permanent(), null, null, null, List.of(), 0, 1).withCyclingTriggerTargets());
        addEffect(EffectSlot.ON_SELF_CYCLED, new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
