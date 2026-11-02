package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "311")
public class ThranTurbine extends Card {

    public ThranTurbine() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new AwardRestrictedManaEffect(ManaColor.COLORLESS, 2, new ManaRestriction.SpellsProhibited()),
                "Add {C}{C}?")
        );
    }
}
