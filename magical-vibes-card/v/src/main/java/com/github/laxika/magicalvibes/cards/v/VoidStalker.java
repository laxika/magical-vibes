package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutOnTopOfLibraryScope;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "77")
public class VoidStalker extends Card {

    public VoidStalker() {
        addActivatedAbility(new ActivatedAbility(true, "{2}{U}",
                List.of(new PutTargetOnTopOfLibraryEffect(PutOnTopOfLibraryScope.SELF_AND_TARGET)),
                "{2}{U}, {T}: Put this creature and target creature on top of their owners' libraries, then those players shuffle their libraries.",
                TargetFilters.creature()));
    }
}
