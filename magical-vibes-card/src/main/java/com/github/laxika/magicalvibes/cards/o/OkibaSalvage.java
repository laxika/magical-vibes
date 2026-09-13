package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "118")
public class OkibaSalvage extends Card {

    public OkibaSalvage() {
        CardPredicate creatureOrVehicle = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSubtypePredicate(CardSubtype.VEHICLE)));
        Condition controlsArtifactAndEnchantment = new AllOf(List.of(
                new ControlsPermanent(new PermanentIsArtifactPredicate()),
                new ControlsPermanent(new PermanentIsEnchantmentPredicate())));

        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(creatureOrVehicle)
                .targetGraveyard(true)
                .plusOneCountersIfCondition(controlsArtifactAndEnchantment)
                .plusOneCounterCount(2)
                .build());
    }
}
