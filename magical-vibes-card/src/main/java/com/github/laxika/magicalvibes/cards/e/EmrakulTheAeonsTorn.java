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

@CardRegistration(set = "ROE", collectorNumber = "4")
public class EmrakulTheAeonsTorn extends Card {

    public EmrakulTheAeonsTorn() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());

        addEffect(EffectSlot.ON_SELF_CAST, new ControllerExtraTurnEffect(1));

        addEffect(EffectSlot.STATIC, new ProtectionFromColoredSpellsEffect());

        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                6, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));

        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new ShuffleGraveyardIntoLibraryEffect(false));
    }
}
