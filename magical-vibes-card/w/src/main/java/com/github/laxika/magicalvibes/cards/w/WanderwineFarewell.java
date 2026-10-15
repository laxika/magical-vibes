package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentsThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "83")
public class WanderwineFarewell extends Card {

    public WanderwineFarewell() {
        target(TargetFilters.nonlandPermanent(), 1, 2)
                .addEffect(EffectSlot.SPELL, new ReturnTargetPermanentsThenEffect(
                        new ConditionalEffect(
                                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)),
                                new CreateTokenEffect(CardType.CREATURE, new EventValue(), "Merfolk", 1, 1,
                                        CardColor.WHITE, Set.of(CardColor.WHITE, CardColor.BLUE),
                                        List.of(CardSubtype.MERFOLK), Set.of(), Set.of(), false, false, Map.of(),
                                        List.of(), false, false, false, 0, Set.of()))));
    }
}
