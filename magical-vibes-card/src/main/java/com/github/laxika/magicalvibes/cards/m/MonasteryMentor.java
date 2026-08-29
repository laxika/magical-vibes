package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FRF", collectorNumber = "20")
public class MonasteryMentor extends Card {

    public MonasteryMentor() {
        // Prowess (Whenever you cast a noncreature spell, this creature gets +1/+1 until end of turn.)
        SpellCastTriggerEffect noncreatureSpellTrigger = new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new BoostSelfEffect(1, 1))
        );
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, noncreatureSpellTrigger);

        // Whenever you cast a noncreature spell, create a 1/1 white Monk creature token with prowess.
        SpellCastTriggerEffect tokenProwessTrigger = new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new BoostSelfEffect(1, 1))
        );
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new CreateTokenEffect(1, "Monk", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.MONK), Set.of(Keyword.PROWESS), Set.of(),
                        Map.of(EffectSlot.ON_CONTROLLER_CASTS_SPELL, tokenProwessTrigger)))
        ));
    }
}
