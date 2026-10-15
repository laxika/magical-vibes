package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CanBlockAndBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayPutOntoBattlefieldShuffleRestEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "139")
@CardRegistration(set = "TLE", collectorNumber = "205")
public class HeiBaiForestGuardian extends Card {

    public HeiBaiForestGuardian() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealUntilCardPredicateMayPutOntoBattlefieldShuffleRestEffect(
                        new CardSubtypePredicate(CardSubtype.SHRINE)));

        var legendaryEnchantments = new PermanentAllOfPredicate(List.of(
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                new PermanentIsEnchantmentPredicate()));
        var spirits = new PermanentHasSubtypePredicate(CardSubtype.SPIRIT);
        var spiritToken = new CreateTokenEffect(
                CardType.CREATURE,
                new PermanentCount(legendaryEnchantments, CountScope.CONTROLLER),
                "Spirit", 1, 1, null, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of(),
                false, false,
                Map.of(
                        EffectSlot.STATIC, new CanBlockAndBeBlockedOnlyByFilterEffect(spirits, "Spirits")
                ),
                List.of(), false, false, false, 0, Set.of());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{U}{B}{R}{G}",
                List.of(spiritToken),
                "{W}{U}{B}{R}{G}, {T}: Create a 1/1 colorless Spirit creature token for each legendary enchantment you control."));
    }
}
