package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "278")
@CardRegistration(set = "6ED", collectorNumber = "261")
public class UktabiWildcats extends Card {

    public UktabiWildcats() {
        // Uktabi Wildcats's power and toughness are each equal to the number of Forests you control.
        PermanentCount forestsYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(forestsYouControl, forestsYouControl));

        // {G}, Sacrifice a Forest: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                                "Sacrifice a Forest",
                                false
                        ),
                        new RegenerateEffect()
                ),
                "{G}, Sacrifice a Forest: Regenerate Uktabi Wildcats."
        ));
    }
}
