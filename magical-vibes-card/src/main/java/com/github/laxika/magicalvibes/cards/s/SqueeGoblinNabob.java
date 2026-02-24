package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "10E", collectorNumber = "239")
public class SqueeGoblinNabob extends Card {

    public SqueeGoblinNabob() {
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new MayEffect(new ReturnCardFromGraveyardEffect(GraveyardChoiceDestination.HAND, new CardIsSelfPredicate(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, true, false, null), "Return Squee, Goblin Nabob from your graveyard to your hand?"));
    }
}
