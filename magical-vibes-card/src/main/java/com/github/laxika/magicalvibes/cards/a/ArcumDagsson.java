package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffect;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "27")
public class ArcumDagsson extends Card {

    public ArcumDagsson() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffect()),
                "{T}: Target artifact creature's controller sacrifices it. That player may search their library for a noncreature artifact card, put it onto the battlefield, then shuffle."
        ));
    }
}
