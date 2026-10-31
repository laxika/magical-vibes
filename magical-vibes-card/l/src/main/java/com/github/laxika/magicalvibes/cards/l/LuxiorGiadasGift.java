package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCardTypeFromAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "240")
public class LuxiorGiadasGift extends Card {

    public LuxiorGiadasGift() {
        addStaticEffect(new AttachedBoostEffect(
                new CountersOnSource(CounterType.ANY),
                new CountersOnSource(CounterType.ANY),
                GrantScope.EQUIPPED_CREATURE,
                true));
        addStaticEffect(new GrantCardTypeEffect(CardType.CREATURE, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new RemoveCardTypeFromAttachedPermanentEffect(CardType.PLANESWALKER));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(EquipEffect.toPlaneswalker()),
                "Equip planeswalker {1}",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        "Target must be a planeswalker you control"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
