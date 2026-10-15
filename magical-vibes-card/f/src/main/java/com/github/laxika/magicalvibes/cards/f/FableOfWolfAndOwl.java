package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "150")
public class FableOfWolfAndOwl extends Card {

    public FableOfWolfAndOwl() {
        // Whenever you cast a green spell, you may create a 2/2 green Wolf creature token.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.GREEN),
                        List.of(new CreateTokenEffect("Wolf", 2, 2, CardColor.GREEN,
                                List.of(CardSubtype.WOLF), Set.of(), Set.of()))),
                "Create a 2/2 green Wolf creature token?"));

        // Whenever you cast a blue spell, you may create a 1/1 blue Bird creature token with flying.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.BLUE),
                        List.of(new CreateTokenEffect("Bird", 1, 1, CardColor.BLUE,
                                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()))),
                "Create a 1/1 blue Bird creature token with flying?"));
    }
}
