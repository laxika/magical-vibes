package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

@CardRegistration(set = "ACR", collectorNumber = "15")
public class CrystalSkullIsuSpyglass extends Card {

    public CrystalSkullIsuSpyglass() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new PlayLandsFromTopOfLibraryEffect(new CardIsHistoricPredicate()));
        addStaticEffect(new AllowCastFromTopOfLibraryEffect(new CardIsHistoricPredicate()));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
