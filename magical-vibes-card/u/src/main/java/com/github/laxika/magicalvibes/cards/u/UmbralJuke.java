package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "89")
public class UmbralJuke extends Card {

    public UmbralJuke() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player sacrifices a creature or planeswalker of their choice",
                        new SacrificePermanentsEffect(1,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate())),
                                SacrificeRecipient.TARGET_PLAYER)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 2/1 white and black Inkling creature token with flying",
                        new CreateTokenEffect(1, "Inkling", 2, 1,
                                CardColor.WHITE, Set.of(CardColor.WHITE, CardColor.BLACK),
                                List.of(CardSubtype.INKLING), Set.of(Keyword.FLYING), Set.of()))
        )));
    }
}
