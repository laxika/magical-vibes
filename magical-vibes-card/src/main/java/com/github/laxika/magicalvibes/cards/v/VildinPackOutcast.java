package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DronepackKindred;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "148")
public class VildinPackOutcast extends Card {

    public VildinPackOutcast() {
        setBackFaceCard(new DronepackKindred());

        addActivatedAbility(new ActivatedAbility(
                false, "{R}", List.of(new BoostSelfEffect(1, -1)),
                "{R}: This creature gets +1/-1 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false, "{5}{R}{R}", List.of(new TransformSelfEffect()),
                "{5}{R}{R}: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "DronepackKindred";
    }
}
