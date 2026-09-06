package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldFaceDownEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "180")
public class DeathmistRaptor extends Card {

    public DeathmistRaptor() {
        addMorph("{4}{G}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(1));
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_PERMANENT_TURNS_FACE_UP, new MayEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Face up",
                                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false)),
                        new ChooseOneEffect.ChooseOneOption("Face down",
                                new ReturnSourceCardFromGraveyardToBattlefieldFaceDownEffect())
                )),
                "Return Deathmist Raptor to the battlefield?"));
    }
}
