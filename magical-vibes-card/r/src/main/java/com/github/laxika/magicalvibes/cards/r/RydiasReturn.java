package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "198")
public class RydiasReturn extends Card {

    public RydiasReturn() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +3/+3 until end of turn",
                        new BoostAllOwnCreaturesEffect(3, 3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return up to two target permanent cards from your graveyard to your hand",
                        new ReturnTargetCardsFromGraveyardToHandEffect(new CardIsPermanentPredicate(), 2))
        )));
    }
}
