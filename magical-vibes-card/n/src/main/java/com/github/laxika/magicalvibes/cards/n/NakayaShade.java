package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "69")
public class NakayaShade extends Card {

    public NakayaShade() {
        // {B}: This creature gets +1/+1 until end of turn unless any player pays {2}.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(UnlessPaysEffect.unlessPays(Subjects.eachPlayer(), "{2}", new BoostSelfEffect(1, 1))),
                "{B}: This creature gets +1/+1 until end of turn unless any player pays {2}."
        ));
    }
}
