package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToControllerAndCreateTokensEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "316")
@CardRegistration(set = "SLD", collectorNumber = "1694")
public class Inkshield extends Card {

    public Inkshield() {
        addEffect(EffectSlot.SPELL, new PreventAllCombatDamageToControllerAndCreateTokensEffect(
                new CreateTokenEffect(1, "Inkling", 2, 1, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.INKLING),
                        Set.of(Keyword.FLYING), Set.of())));
    }
}
