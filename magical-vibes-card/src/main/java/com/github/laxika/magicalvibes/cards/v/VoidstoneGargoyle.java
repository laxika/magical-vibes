package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "21")
public class VoidstoneGargoyle extends Card {

    public VoidstoneGargoyle() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCardNameOnEnterEffect(List.of(CardType.LAND)));
        addEffect(EffectSlot.STATIC, new SpellsWithChosenNameCantBeCastEffect());
        addEffect(EffectSlot.STATIC, new ActivatedAbilitiesOfChosenNameCantBeActivatedEffect(true));
    }
}
