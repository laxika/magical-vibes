package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "27")
public class JukaiLiberator extends Card {

    public JukaiLiberator() {
        addNinjutsu("{1}{G}");

        CardAllOfPredicate nonlandPermanent = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Land",
                        new SeekEffect(new CardTypePredicate(CardType.LAND))),
                new ChooseOneEffect.ChooseOneOption(
                        "Nonland",
                        new SeekEffect(nonlandPermanent))
        )));
    }
}
