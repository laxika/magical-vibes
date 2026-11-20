package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "24")
public class KhodEtlanShiisEnvoy extends Card {

    public KhodEtlanShiisEnvoy() {
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.ISLAND, Subjects.allLands()));
        addStaticEffect(new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.BLUE), Subjects.allLands()));
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.otherCreaturesYouControl(),
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.HOMARID,
                        CardSubtype.CAMARID,
                        CardSubtype.CEPHALID,
                        CardSubtype.NAUTILID,
                        CardSubtype.MERFOLK))));
    }
}
