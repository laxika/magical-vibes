package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BringBack;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "212")
public class OakhameRangerBringBack extends Card {

    public OakhameRangerBringBack() {
        setBackFaceCard(new BringBack());
        addCastingOption(new AdventureCast("{G/W}{G/W}{G/W}{G/W}"));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "{T}: Creatures you control get +1/+1 until end of turn."
        ));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "BringBack";
    }
}
