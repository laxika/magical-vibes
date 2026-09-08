package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentColorCount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerSpellsCantBeCounteredThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "149")
public class BoundDetermined extends Card {

    public BoundDetermined() {
        CardEffect bound = new SacrificePermanentThenEffect(
                new PermanentIsCreaturePredicate(),
                new ReturnCardsFromControllerGraveyardToHandEffect(
                        new CardTruePredicate(), new SacrificedPermanentColorCount()),
                "a creature", false, false);
        CardEffect determined = new GrantControllerSpellsCantBeCounteredThisTurnEffect();

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Bound — Sacrifice a creature. Return up to X cards from your graveyard to your hand, where X is the number of colors that creature was. Exile this card.",
                        List.of(bound, new ExileSpellEffect())
                ).withManaCost("{3}{B}{G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Determined — Other spells you control can't be countered this turn. Draw a card.",
                        List.of(determined, new DrawCardEffect())
                ).withManaCost("{G}{U}")
        )));
    }
}
