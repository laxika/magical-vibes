package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EntwineManaCost;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "205")
public class KayasGuile extends Card {

    public KayasGuile() {
        addEffect(EffectSlot.SPELL, new EntwineManaCost("{3}"));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent sacrifices a creature of their choice",
                        new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                SacrificeRecipient.EACH_OPPONENT)),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile all opponents' graveyards",
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_OPPONENTS)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 white and black Spirit creature token with flying",
                        new CreateTokenEffect(1, "Spirit", 1, 1, CardColor.WHITE,
                                Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.SPIRIT),
                                Set.of(Keyword.FLYING), Set.of())),
                new ChooseOneEffect.ChooseOneOption(
                        "You gain 4 life",
                        new GainLifeEffect(4))
        ), false, 2, 4));
    }
}
