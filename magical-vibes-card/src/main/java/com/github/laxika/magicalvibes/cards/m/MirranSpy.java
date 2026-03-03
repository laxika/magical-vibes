package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetCreatureOnOwnSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MBS", collectorNumber = "26")
public class MirranSpy extends Card {

    public MirranSpy() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new UntapTargetCreatureOnOwnSpellCastEffect(new CardTypePredicate(CardType.ARTIFACT)),
                "Untap target creature?"
        ));
    }
}
