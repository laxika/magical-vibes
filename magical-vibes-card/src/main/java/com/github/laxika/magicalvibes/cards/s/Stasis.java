package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "127")
@CardRegistration(set = "4ED", collectorNumber = "104")
public class Stasis extends Card {

    public Stasis() {
        // Players skip their untap steps — no permanent, any controller, untaps.
        addEffect(EffectSlot.STATIC, new MatchingPermanentsDoesntUntapEffect(new PermanentTruePredicate()));

        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {U}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{U}"),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
