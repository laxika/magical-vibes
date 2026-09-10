package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastingPlayerMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "OHOP", collectorNumber = "24")
public class Minamo extends Card {

    public Minamo() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(new CastingPlayerMayDrawCardEffect())));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new EachPlayerMayReturnCardFromGraveyardToHandEffect(new CardColorPredicate(CardColor.BLUE)));
    }
}
