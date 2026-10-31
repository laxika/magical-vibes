package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "215")
public class GlarbCalamitysAugur extends Card {

    public GlarbCalamitysAugur() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new PlayLandsFromTopOfLibraryEffect());
        addStaticEffect(new AllowCastFromTopOfLibraryEffect(new CardMinManaValuePredicate(4, true)));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SurveilEffect(2)),
                "{T}: Surveil 2."
        ));
    }
}
