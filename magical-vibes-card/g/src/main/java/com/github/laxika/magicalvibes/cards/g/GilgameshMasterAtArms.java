package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachOneOfEquipmentToSamuraiEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "139")
@CardRegistration(set = "FIN", collectorNumber = "338")
@CardRegistration(set = "FIN", collectorNumber = "461")
public class GilgameshMasterAtArms extends Card {

    public GilgameshMasterAtArms() {
        LookAtTopCardsEffect search =
                LookAtTopCardsEffect.mayPutAnyNumberMatchingOntoBattlefieldRestOnBottomRandom(
                        6,
                        new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                        new AttachOneOfEquipmentToSamuraiEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), search);
        addEffect(EffectSlot.ON_ATTACK, search);
    }
}
