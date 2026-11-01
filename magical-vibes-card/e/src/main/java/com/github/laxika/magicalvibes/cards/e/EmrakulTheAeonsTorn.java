package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColoredSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "4")
@CardRegistration(set = "MM2", collectorNumber = "3")
@CardRegistration(set = "UMA", collectorNumber = "4")
@CardRegistration(set = "2X2", collectorNumber = "1")
@CardRegistration(set = "AA3", collectorNumber = "1")
public class EmrakulTheAeonsTorn extends Card {

    public EmrakulTheAeonsTorn() {
        addStaticEffect(new CantBeCounteredEffect());

        addEffect(EffectSlot.ON_SELF_CAST, new ControllerExtraTurnEffect(1));

        addStaticEffect(new ProtectionFromColoredSpellsEffect());

        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                6, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));

        addTrigger(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS),
                new ShuffleGraveyardIntoLibraryEffect(false));
    }
}
