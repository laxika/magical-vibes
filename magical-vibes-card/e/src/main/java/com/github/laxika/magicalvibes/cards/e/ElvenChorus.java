package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;

import java.util.Set;

@CardRegistration(set = "HOC", collectorNumber = "26")
@CardRegistration(set = "HOC", collectorNumber = "66")
public class ElvenChorus extends Card {

    public ElvenChorus() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new AllowCastFromTopOfLibraryEffect(Set.of(CardType.CREATURE)));
        addStaticEffect(new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(), GrantScope.ALL_OWN_CREATURES));
    }
}
