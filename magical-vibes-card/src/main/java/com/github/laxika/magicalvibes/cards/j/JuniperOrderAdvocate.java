package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "6")
public class JuniperOrderAdvocate extends Card {

    public JuniperOrderAdvocate() {
        // As long as this creature is untapped, green creatures you control get +1/+1.
        // ALL_OWN_CREATURES (not OWN_CREATURES) so the source would buff itself if it
        // were ever green; the untapped gate is re-evaluated each static pass.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceUntapped(),
                new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES,
                        new PermanentColorInPredicate(Set.of(CardColor.GREEN)))));
    }
}
