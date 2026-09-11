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
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "91")
public class DefiledCryptCadaverLab extends Card {

    public DefiledCryptCadaverLab() {
        setRoomDoorManaCosts(List.of("{3}{B}", "{B}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Defiled Crypt", List.of())
                        .withManaCost("{3}{B}"),
                new ChooseOneEffect.ChooseOneOption("Cadaver Lab", List.of())
                        .withManaCost("{B}")
        )));

        CreateTokenEffect horrorToken = new CreateTokenEffect(1, "Horror", 2, 2,
                CardColor.BLACK, List.of(CardSubtype.HORROR), Set.of(), Set.of(CardType.ENCHANTMENT));
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD,
                new OncePerTurnTriggerEffect(horrorToken));

        ReturnCardFromGraveyardEffect returnCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();
        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(1, returnCreature));
    }
}
