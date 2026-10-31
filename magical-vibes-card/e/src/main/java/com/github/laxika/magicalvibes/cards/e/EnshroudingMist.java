package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsRenownedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ORI", collectorNumber = "13")
public class EnshroudingMist extends Card {

    public EnshroudingMist() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(1, 1))
                .addSpellEffect(PreventDamageEffect.allToTargetCreatures())
                // If it's renowned (CR 702.112b), untap it.
                .addSpellEffect(new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentIsRenownedPredicate()),
                        new UntapPermanentsEffect(TapUntapScope.TARGET)));
    }
}
