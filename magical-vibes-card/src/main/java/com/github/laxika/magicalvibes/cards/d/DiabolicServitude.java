package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndReturnSourceToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "USG", collectorNumber = "130")
@CardRegistration(set = "PD3", collectorNumber = "22")
@CardRegistration(set = "TD2", collectorNumber = "75")
public class DiabolicServitude extends Card {

    public DiabolicServitude() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .linkToSource(true)
                .build());
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new ExileTriggeringCreatureAndReturnSourceToHandEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.EXILE));
    }
}
