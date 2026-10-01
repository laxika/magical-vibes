package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.Set;

@CardRegistration(set = "YBRO", collectorNumber = "13")
public class FoundryGroundbreaker extends Card {

    public FoundryGroundbreaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER),
                new ConjureCardNamedOntoBattlefieldEffect("BRO", "265", Set.of(CardType.LAND)),
                new ConjureCardNamedOntoBattlefieldEffect("BRO", "265", Set.of(CardType.LAND))));
    }
}
