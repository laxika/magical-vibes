package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "209")
public class MournersShield extends Card {

    public MournersShield() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, true).imprintingOnSource(),
                "Exile target card from a graveyard?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(PreventDamageFromChosenSourceEffect.allDamageFromSourceSharingColorWithImprintedCard()),
                "Prevent all damage that would be dealt this turn by a source of your choice that shares a color with the exiled card."
        ));
    }
}
