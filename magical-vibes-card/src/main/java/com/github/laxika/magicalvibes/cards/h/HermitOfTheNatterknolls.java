package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LoneWolfOfTheNatterknolls;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "209")
public class HermitOfTheNatterknolls extends Card {

    public HermitOfTheNatterknolls() {
        setBackFaceCard(new LoneWolfOfTheNatterknolls());

        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                SpellCastTriggerEffect.duringYourTurn(null, List.of(new DrawCardEffect(1))));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "LoneWolfOfTheNatterknolls";
    }
}
