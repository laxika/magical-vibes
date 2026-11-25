package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "172")
@CardRegistration(set = "2X2", collectorNumber = "214")
public class FirebladeArtist extends Card {

    public FirebladeArtist() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(
                                DealDamageEffect.toTargetOpponentOrPlaneswalker(2)))),
                "Sacrifice a creature?"));
    }
}
