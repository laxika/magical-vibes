package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "INV", collectorNumber = "238")
public class CauldronDance extends Card {

    public CauldronDance() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.ONLY_DURING_COMBAT);

        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .grantHaste(true)
                .returnToHandAtEndStep(true)
                .build());

        addEffect(EffectSlot.SPELL, new MayEffect(
                new PutCardToBattlefieldEffect(
                        new CardTypePredicate(CardType.CREATURE), "creature", false, false, true, true),
                "Put a creature card from your hand onto the battlefield?"));
    }
}
