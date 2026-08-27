package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "262")
public class LunaticPandora extends Card {

    public LunaticPandora() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SurveilEffect(1)),
                "{2}, {T}: Surveil 1."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{6}, {T}, Sacrifice Lunatic Pandora: Destroy target nonland permanent.",
                TargetFilters.nonlandPermanent()
        ));
    }
}
