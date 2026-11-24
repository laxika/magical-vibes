package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "79")
@CardRegistration(set = "SLD", collectorNumber = "1187")
@CardRegistration(set = "FCA", collectorNumber = "7")
@CardRegistration(set = "MB2", collectorNumber = "39")
public class BolassCitadel extends Card {

    public BolassCitadel() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new PlayLandsFromTopOfLibraryEffect());
        addStaticEffect(AllowCastFromTopOfLibraryEffect.anySpell().payingLifeEqualToManaValue());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(10,
                                new PermanentNotPredicate(new PermanentIsLandPredicate())),
                        new LoseLifeEffect(10, Subjects.eachOpponent())
                ),
                "{T}, Sacrifice ten nonland permanents: Each opponent loses 10 life."
        ));
    }
}
