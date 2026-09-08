package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureCardInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "184")
public class LazavTheMultifarious extends Card {

    public LazavTheMultifarious() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(new BecomeCopyOfTargetCreatureCardInGraveyardEffect()),
                "{X}: Lazav becomes a copy of target creature card in your graveyard with mana value X, "
                        + "except its name is Lazav, the Multifarious, it's legendary in addition to its other "
                        + "types, and it has this ability."
        ));
    }
}
