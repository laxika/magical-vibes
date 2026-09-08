package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "206")
public class InfestationExpert extends Card {

    private static final CreateTokenEffect CREATE_INSECT = new CreateTokenEffect(
            "Insect", 1, 1, CardColor.GREEN, List.of(CardSubtype.INSECT), Set.of(), Set.of());

    public InfestationExpert() {
        setBackFaceCard(new InfestedWerewolf());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CREATE_INSECT);
        addEffect(EffectSlot.ON_ATTACK, CREATE_INSECT);
    }

    @Override
    public String getBackFaceClassName() {
        return "InfestedWerewolf";
    }
}
