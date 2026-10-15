package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.PreventXDamageToControllerAndPermanentsAndRedirectToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "17")
public class RefractionTrap extends Card {

    public RefractionTrap() {
        CardPredicate redInstantOrSorcery = new CardAllOfPredicate(List.of(
                        new CardColorPredicate(CardColor.RED),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{W}")),
                new OpponentCastSpellThisTurn(redInstantOrSorcery), false));
        addEffect(EffectSlot.SPELL,
                new PreventXDamageToControllerAndPermanentsAndRedirectToAnyTargetEffect(new Fixed(3)));
    }
}
