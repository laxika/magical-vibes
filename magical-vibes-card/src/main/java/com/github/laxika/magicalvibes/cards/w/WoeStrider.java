package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "123")
public class WoeStrider extends Card {

    public WoeStrider() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                1, "Goat", 0, 1, CardColor.WHITE, List.of(CardSubtype.GOAT), Set.of(), Set.of()));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new SacrificeCreatureCost(false, false, false, true), new ScryEffect(1)),
                "Sacrifice another creature: Scry 1."));

        addCastingOption(new GraveyardCast(null, "{3}{B}{B}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 4))));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));
    }
}
