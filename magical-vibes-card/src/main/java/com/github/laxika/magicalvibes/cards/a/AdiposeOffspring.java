package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentToughness;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "10")
@CardRegistration(set = "WHO", collectorNumber = "333")
public class AdiposeOffspring extends Card {

    public AdiposeOffspring() {
        // Emerge {5}{W} — sacrifice a creature and pay the emerge cost reduced by that creature's
        // mana value (generic only; colored components cannot be reduced).
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{5}{W}"),
                new SacrificePermanentsCost(1, new PermanentIsCreaturePredicate())
        ), true));

        CreateTokenEffect oneAlien = new CreateTokenEffect(
                1, "Alien", 2, 2, CardColor.WHITE, List.of(CardSubtype.ALIEN),
                java.util.Set.of(), java.util.Set.of());
        CreateTokenEffect emergeAliens = new CreateTokenEffect(
                new SacrificedPermanentToughness(), "Alien", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.ALIEN), java.util.Set.of(), java.util.Set.of());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new NotCondition(new CastForAlternateCost()), oneAlien));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new CastForAlternateCost(), emergeAliens));
    }
}
