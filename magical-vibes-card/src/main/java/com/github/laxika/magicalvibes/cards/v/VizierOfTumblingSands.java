package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "75")
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

        // Cycling {1}{U} ({1}{U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, untap target permanent." The reflexive trigger rides on the
        // cycling ability: its target permanent is chosen at activation, the untap resolves, then the
        // cycling draw resumes. Targeting is auto-derived from UntapPermanentsEffect's TargetSpec.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{U}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET), new DrawCardEffect(1)),
                "Cycling {1}{U} ({1}{U}, Discard this card: Draw a card.)"));
    }
}
