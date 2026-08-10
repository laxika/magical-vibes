package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellweaverHelixTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MRD", collectorNumber = "247")
public class SpellweaverHelix extends Card {

    public SpellweaverHelix() {
        CardPredicate sorcery = new CardTypePredicate(CardType.SORCERY);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(2, sorcery, true),
                "You may exile two target sorcery cards from a single graveyard."
        ));
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellweaverHelixTriggerEffect());
    }
}
