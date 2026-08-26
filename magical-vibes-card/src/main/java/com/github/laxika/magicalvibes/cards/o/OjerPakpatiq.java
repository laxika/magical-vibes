package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TempleOfCyclicalTime;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LCI", collectorNumber = "67")
public class OjerPakpatiq extends Card {

    public OjerPakpatiq() {
        setBackFaceCard(new TempleOfCyclicalTime());

        addEffect(EffectSlot.STATIC, new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.REBOUND, new CardTypePredicate(CardType.INSTANT)));
        addEffect(EffectSlot.ON_DEATH,
                new ReturnSourceTransformedFromGraveyardEffect(true, CounterType.TIME, 3, true));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "TempleOfCyclicalTime";
    }
}
