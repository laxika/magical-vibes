package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.DyingPermanentWasCreatureConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;

import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "176")
public class EnduringVitality extends Card {

    public EnduringVitality() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(), GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.ON_DEATH, new DyingPermanentWasCreatureConditionalEffect(
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false, Set.of(CardType.ENCHANTMENT))));
    }
}
