package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DyingPermanentWasCreatureConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;

import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "133")
public class EnduringCourage extends Card {

    public EnduringCourage() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new BoostEnteringCreatureEffect(2, 0, Set.of(Keyword.HASTE)));
        addEffect(EffectSlot.ON_DEATH, new DyingPermanentWasCreatureConditionalEffect(
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false, Set.of(CardType.ENCHANTMENT))));
    }
}
