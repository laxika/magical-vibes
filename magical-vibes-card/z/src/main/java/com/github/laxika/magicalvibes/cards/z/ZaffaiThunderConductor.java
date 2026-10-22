package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C21", collectorNumber = "4")
public class ZaffaiThunderConductor extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));
    private static final CreateTokenEffect ELEMENTAL_TOKEN = new CreateTokenEffect(
            "Elemental", 4, 4, CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED),
            List.of(CardSubtype.ELEMENTAL));

    public ZaffaiThunderConductor() {
        // Magecraft is a single trigger: scry 1, then the mana-value-gated Elemental and damage, in order.
        List<CardEffect> magecraft = List.of(
                new ScryEffect(1),
                new ConditionalEffect(new EventValueAtLeast(5), ELEMENTAL_TOKEN),
                new ConditionalEffect(new EventValueAtLeast(10), new DealDamageToRandomOpponentEffect(10)));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(INSTANT_OR_SORCERY, magecraft));
        addEffect(EffectSlot.ON_CONTROLLER_COPIES_SPELL, new SpellCopyTriggerEffect(INSTANT_OR_SORCERY, magecraft));
    }
}
