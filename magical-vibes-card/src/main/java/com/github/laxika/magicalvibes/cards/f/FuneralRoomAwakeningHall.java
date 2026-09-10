package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "100")
public class FuneralRoomAwakeningHall extends Card {

    public FuneralRoomAwakeningHall() {
        setRoomDoorManaCosts(List.of("{2}{B}", "{6}{B}{B}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Funeral Room", List.of())
                        .withManaCost("{2}{B}"),
                new ChooseOneEffect.ChooseOneOption("Awakening Hall", List.of())
                        .withManaCost("{6}{B}{B}")
        )));

        SequenceEffect drain = SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(1));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, drain);

        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(1,
                        new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                                new CardTypePredicate(CardType.CREATURE), Integer.MAX_VALUE)));
    }
}
