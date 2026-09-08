package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentOrDiscardsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "82")
public class Plaguecrafter extends Card {

    public Plaguecrafter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EachPlayerSacrificesPermanentOrDiscardsEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()))));
    }
}
