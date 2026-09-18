package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RingTemptsYouEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "36")
@CardRegistration(set = "HOC", collectorNumber = "76")
public class SauronTheDarkLord extends Card {

    public SauronTheDarkLord() {
        PermanentPredicate legendaryArtifactOrCreature = new PermanentAllOfPredicate(List.of(
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()))));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessSacrificesEffect(
                        legendaryArtifactOrCreature,
                        "legendary artifact or legendary creature"));

        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastTriggerEffect(null,
                        List.of(new AmassGoblinsEffect(1, CardSubtype.ORC))));

        PermanentPredicate army = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ARMY)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(army, new RingTemptsYouEffect()));

        CardEffect discardAndDraw = new DiscardOwnHandThenDrawEffect(new Fixed(4));
        addEffect(EffectSlot.ON_RING_TEMPTS_YOU,
                new MayEffect(discardAndDraw, "Discard your hand and draw four cards?"));
    }
}
