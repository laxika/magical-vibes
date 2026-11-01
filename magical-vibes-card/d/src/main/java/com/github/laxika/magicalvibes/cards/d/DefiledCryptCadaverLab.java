package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "91")
public class DefiledCryptCadaverLab extends Card {

    public DefiledCryptCadaverLab() {
        setRoomDoorManaCosts(List.of("{3}{B}", "{B}"));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Defiled Crypt", List.of())
                        .withManaCost("{3}{B}"),
                new ChooseOneEffect.ChooseOneOption("Cadaver Lab", List.of())
                        .withManaCost("{B}")
        )));

        CreateTokenEffect horrorToken = new CreateTokenEffect(1, "Horror", 2, 2,
                CardColor.BLACK, List.of(CardSubtype.HORROR), Set.of(), Set.of(CardType.ENCHANTMENT));
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                new OncePerTurnTriggerEffect(
                        new ConditionalEffect(new SourceRoomDoorUnlocked(0), horrorToken, true)));

        ReturnCardFromGraveyardEffect returnCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();
        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(1, returnCreature));
    }
}
