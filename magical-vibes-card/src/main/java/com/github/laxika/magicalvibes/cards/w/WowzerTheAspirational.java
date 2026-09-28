package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.condition.ControllerHasInitiative;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "365")
@CardRegistration(set = "MB2", collectorNumber = "604")
public class WowzerTheAspirational extends Card {

    public WowzerTheAspirational() {
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(new AllOf(List.of(
                        new ControllerEnergyAtLeast(1),
                        controlsToken(CardSubtype.BLOOD),
                        controlsToken(CardSubtype.CLUE),
                        controlsToken(CardSubtype.FOOD),
                        controlsToken(CardSubtype.MAP),
                        controlsToken(CardSubtype.POWERSTONE),
                        controlsToken(CardSubtype.TREASURE),
                        new ControllerIsMonarch(),
                        new ControllerHasCityBlessing(),
                        new ControllerHasInitiative()
                )), new WinGameEffect()));
    }

    private static ControlsPermanent controlsToken(CardSubtype subtype) {
        return new ControlsPermanent(new PermanentHasSubtypePredicate(subtype));
    }
}
