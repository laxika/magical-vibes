package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "79")
@CardRegistration(set = "MSC", collectorNumber = "397")
public class CosmicCrucible extends Card {

    public CosmicCrucible() {
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), new AwardAnyColorManaEffect(4, true));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, OncePerTurnTriggerEffect.markOnAcceptance(
                new MayEffect(
                        CopyControllerCastSpellOnSpellCastEffect.permanentSpellToken(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                        "Copy that spell?")));
    }
}
