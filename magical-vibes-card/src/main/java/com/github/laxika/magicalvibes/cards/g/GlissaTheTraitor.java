package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MBS", collectorNumber = "96")
public class GlissaTheTraitor extends Card {

    public GlissaTheTraitor() {
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new MayEffect(
                        new ReturnCardFromGraveyardEffect(GraveyardChoiceDestination.HAND, new CardTypePredicate(CardType.ARTIFACT)),
                        "Return an artifact card from your graveyard to your hand?"));
    }
}
