package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "118")
public class MyrWelder extends Card {

    public MyrWelder() {
        // Imprint — {T}: Exile target artifact card from a graveyard.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new ExileTargetCardFromGraveyardAndImprintOnSourceEffect(new CardTypePredicate(CardType.ARTIFACT))),
                "{T}: Exile target artifact card from a graveyard."
        ));

        // Myr Welder has all activated abilities of all cards exiled with it.
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfExiledCardsEffect());
    }
}
