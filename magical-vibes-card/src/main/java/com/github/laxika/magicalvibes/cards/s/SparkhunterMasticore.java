package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "240")
public class SparkhunterMasticore extends Card {

    public SparkhunterMasticore() {
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null));
        addEffect(EffectSlot.STATIC,
                new ProtectionFromCardTypesEffect(Set.of(CardType.PLANESWALKER)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                        1, new PermanentIsPlaneswalkerPredicate())),
                "{1}: This creature deals 1 damage to target planeswalker."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)),
                "{3}: This creature gains indestructible until end of turn."
        ));
    }
}
