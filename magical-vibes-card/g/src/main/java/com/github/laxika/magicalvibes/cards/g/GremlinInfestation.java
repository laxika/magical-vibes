package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "83")
public class GremlinInfestation extends Card {

    public GremlinInfestation() {
        target(TargetFilters.artifact())
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                        new DealDamageToEnchantedPermanentControllerEffect(2))
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new CreateTokenEffect("Gremlin", 2, 2, CardColor.RED,
                                List.of(CardSubtype.GREMLIN), Set.of(), Set.of()));
    }
}
