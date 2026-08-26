package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "223")
public class AnimPakalThousandthMoon extends Card {

    public AnimPakalThousandthMoon() {
        CreateTokenEffect gnome = new CreateTokenEffect(
                CardType.CREATURE,
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                "Gnome",
                1,
                1,
                null,
                null,
                List.of(CardSubtype.GNOME),
                Set.of(),
                Set.of(CardType.ARTIFACT),
                true,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()
        );
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new HasAttacker(new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.GNOME))),
                SequenceEffect.of(
                        new PutCountersOnSourceEffect(1, 1, 1),
                        gnome
                )));
    }
}
