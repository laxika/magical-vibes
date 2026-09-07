package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "9")
public class TheCircleOfLoyalty extends Card {

    public TheCircleOfLoyalty() {
        // Affinity for Knights.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.KNIGHT), CountScope.CONTROLLER)));

        // Creatures you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));

        CreateTokenEffect knightToken = new CreateTokenEffect(
                "Knight", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of());

        // Whenever you cast a legendary spell, create a 2/2 white Knight creature token with vigilance.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardSupertypePredicate(CardSupertype.LEGENDARY), List.of(knightToken)));

        // {3}{W}, {T}: Create a 2/2 white Knight creature token with vigilance.
        addActivatedAbility(new ActivatedAbility(
                true, "{3}{W}", List.of(knightToken),
                "{3}{W}, {T}: Create a 2/2 white Knight creature token with vigilance."));
    }
}
