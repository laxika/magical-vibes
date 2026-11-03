package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "1")
public class AuroraEidolon extends Card {

    public AuroraEidolon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new SacrificeSelfCost(), PreventDamageEffect.nextToTarget(3)),
                "{W}, Sacrifice this creature: Prevent the next 3 damage that would be dealt to any target this turn."
        ));

        addTrigger(new TriggerCondition(Triggers.youCastSpell().withFilter(new CardIsMulticoloredPredicate()), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new MayEffect(
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Return Aurora Eidolon from your graveyard to your hand?"));
    }
}
