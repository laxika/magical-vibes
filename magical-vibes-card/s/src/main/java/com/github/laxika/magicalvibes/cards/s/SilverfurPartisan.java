package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "228")
public class SilverfurPartisan extends Card {

    public SilverfurPartisan() {
        addTrigger(Triggers.creatureYouControlBecomesTargetOfInstantOrSorcerySpell(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WOLF, CardSubtype.WEREWOLF)),
                        new CreateTokenEffect("Wolf", 2, 2, CardColor.GREEN,
                                List.of(CardSubtype.WOLF), Set.of(), Set.of())));
    }
}
