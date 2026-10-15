package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCreatureAndManifestEffect;

@CardRegistration(set = "YTDM", collectorNumber = "21")
public class HamzaMightOfTheYathan extends Card {

    public HamzaMightOfTheYathan() {
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP,
                EndureEffect.forTriggeringPermanent(new TriggeringPermanentToughness()));

        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new SeekCreatureAndManifestEffect());
    }
}
