package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "46")
public class ReleaseToTheWind extends Card {

    public ReleaseToTheWind() {
        target(TargetFilters.nonlandPermanent())
                .addSpellEffect(new ExileTargetPermanentEffect());
        addSpellEffect(AllowPlayCardsFromExileEffect.whileExiled(Subjects.cardsThisWay(StepKind.EXILE))
                .withoutPayingManaCost()
                .playableByOwner());
    }
}
