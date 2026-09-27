package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "NCC", collectorNumber = "35")
@CardRegistration(set = "NCC", collectorNumber = "136")
public class DoggedDetective extends Card {

    public DoggedDetective() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(2));
        addEffect(EffectSlot.GRAVEYARD_ON_OPPONENT_DRAWS_SECOND_CARD,
                new MayEffect(
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Return Dogged Detective from your graveyard to your hand?"));
    }
}
