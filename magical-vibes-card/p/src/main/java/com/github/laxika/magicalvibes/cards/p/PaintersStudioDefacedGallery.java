package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "147")
public class PaintersStudioDefacedGallery extends Card {

    public PaintersStudioDefacedGallery() {
        setRoomDoorManaCosts(List.of("{2}{R}", "{1}{R}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Painter's Studio", List.of())
                        .withManaCost("{2}{R}"),
                new ChooseOneEffect.ChooseOneOption("Defaced Gallery", List.of())
                        .withManaCost("{1}{R}")
        )));

        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(0,
                        new ExileTopCardsMayPlayUntilNextTurnEffect(2)));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new BoostAllOwnCreaturesEffect(1, 0, new PermanentIsAttackingPredicate()));
    }
}
