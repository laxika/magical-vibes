package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.condition.Coven;

import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "168")
@CardRegistration(set = "DBL", collectorNumber = "168")
@CardRegistration(set = "DSC", collectorNumber = "171")
@CardRegistration(set = "EOC", collectorNumber = "92")
public class AugurOfAutumn extends Card {

    public AugurOfAutumn() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new PlayLandsFromTopOfLibraryEffect());
        addStaticEffect(new ConditionalEffect(
                new Coven(), new AllowCastFromTopOfLibraryEffect(Set.of(CardType.CREATURE))));
    }
}
