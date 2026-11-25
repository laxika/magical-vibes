package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "152")
public class LightningDart extends Card {

    public LightningDart() {
        target(TargetFilters.creature()).addSpellEffect(new ConditionalReplacementEffect(
                new TargetPermanentMatches(new PermanentColorInPredicate(Set.of(CardColor.WHITE, CardColor.BLUE))),
                DealDamageEffect.toTargetCreature(1),
                DealDamageEffect.toTargetCreature(4)
        ));
    }
}
