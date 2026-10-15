package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetBasePowerToughnessToAmountUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "227")
public class MinscBelovedRanger extends Card {

    public MinscBelovedRanger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(CardType.CREATURE, 1, "Boo", 1, 1, CardColor.RED, null,
                        List.of(CardSubtype.HAMSTER), Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of(),
                        false, false, Map.of(), List.of(), false, false, true, 0, Set.of()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(
                        new SetTargetBasePowerToughnessToAmountUntilEndOfTurnEffect(new XValue(), new XValue()),
                        new GrantSubtypeUntilEndOfTurnEffect(CardSubtype.GIANT, GrantScope.TARGET)),
                "{X}: Until end of turn, target creature you control has base power and toughness X/X and becomes a Giant in addition to its other types. Activate only as a sorcery.",
                TargetFilters.creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
