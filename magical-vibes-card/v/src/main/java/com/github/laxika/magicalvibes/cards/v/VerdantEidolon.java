package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "100")
@CardRegistration(set = "UMA", collectorNumber = "190")
public class VerdantEidolon extends Card {

    public VerdantEidolon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect(3)),
                "{G}, Sacrifice this creature: Add three mana of any one color."
        ));

        addTrigger(new TriggerCondition(Triggers.youCastSpell().withFilter(new CardIsMulticoloredPredicate()), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new MayEffect(
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Return Verdant Eidolon from your graveyard to your hand?"));
    }
}
