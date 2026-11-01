package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2308")
@CardRegistration(set = "WHO", collectorNumber = "226")
@CardRegistration(set = "WHO", collectorNumber = "474")
@CardRegistration(set = "WHO", collectorNumber = "817")
@CardRegistration(set = "WHO", collectorNumber = "1065")
@CardRegistration(set = "SOC", collectorNumber = "242")
@CardRegistration(set = "C21", collectorNumber = "50")
@CardRegistration(set = "OTC", collectorNumber = "161")
@CardRegistration(set = "BRC", collectorNumber = "115")
@CardRegistration(set = "FDC", collectorNumber = "149")
public class CursedMirror extends Card {

    public CursedMirror() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addStaticEffect(CopyPermanentOnEnterEffect.temporaryCopy(
                new PermanentIsCreaturePredicate(), "creature", Set.of(Keyword.HASTE)));
    }
}
