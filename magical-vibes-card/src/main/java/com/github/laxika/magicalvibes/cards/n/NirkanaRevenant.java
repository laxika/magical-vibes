package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfSubtypeTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "120")
public class NirkanaRevenant extends Card {

    public NirkanaRevenant() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddManaWhenLandOfSubtypeTappedForManaEffect(CardSubtype.SWAMP, ManaColor.BLACK, true));

        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new BoostSelfEffect(1, 1)),
                "{B}: This creature gets +1/+1 until end of turn."));
    }
}
