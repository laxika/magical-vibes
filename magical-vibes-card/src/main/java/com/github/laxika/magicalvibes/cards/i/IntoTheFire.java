package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutAnyNumberCardsFromHandOnBottomOfLibraryThenDrawThatManyPlusOneEffect;
import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "144")
public class IntoTheFire extends Card {

    public IntoTheFire() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Into the Fire deals 2 damage to each creature, planeswalker, and battle",
                        MassDamageEffect.damageToEachCreaturePlaneswalkerAndBattle(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put any number of cards from your hand on the bottom of your library, then draw that many cards plus one",
                        new PutAnyNumberCardsFromHandOnBottomOfLibraryThenDrawThatManyPlusOneEffect())
        )));
    }
}
