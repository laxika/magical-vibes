package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "107")
public class EsperSojourners extends Card {

    public EsperSojourners() {
        // When this creature dies, you may tap or untap target permanent. The explicit any-permanent
        // filter overrides the death pipeline's creatures-only default so lands/artifacts stay legal.
        target(new PermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent"
        ))
                .addEffect(EffectSlot.ON_DEATH, new TapOrUntapTargetPermanentEffect());

        // Cycling {2}{U} ({2}{U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, you may tap or untap target permanent." The reflexive cycle trigger
        // rides on the cycling ability (Resounding Scream pattern): its target permanent is chosen at
        // activation, the tap/untap resolves, then the cycling draw resumes.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}{U}",
                List.of(new TapOrUntapTargetPermanentEffect(), new DrawCardEffect(1)),
                "Cycling {2}{U} ({2}{U}, Discard this card: Draw a card.)",
                new PermanentPredicateTargetFilter(
                        new PermanentTruePredicate(),
                        "Target must be a permanent")));
    }
}
