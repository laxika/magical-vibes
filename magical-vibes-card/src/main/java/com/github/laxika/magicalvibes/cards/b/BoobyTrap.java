package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoobyTrapEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "289")
public class BoobyTrap extends Card {

    public BoobyTrap() {
        // "As this artifact enters, choose an opponent and a card name other than a basic land card name."
        // The chosen opponent is the (single) opponent; the name is stamped onto the permanent.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardNameOnEnterEffect(List.of(CardType.LAND)));
        // "The chosen player reveals each card they draw." + the draw trigger — handled in DrawService.
        addEffect(EffectSlot.STATIC, new BoobyTrapEffect());
    }
}
