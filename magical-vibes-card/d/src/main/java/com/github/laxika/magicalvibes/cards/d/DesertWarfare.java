package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "64")
@CardRegistration(set = "M3C", collectorNumber = "98")
@CardRegistration(set = "M3C", collectorNumber = "116")
public class DesertWarfare extends Card {

    public DesertWarfare() {
        CardSubtypePredicate desertCard = new CardSubtypePredicate(CardSubtype.DESERT);
        RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect returnDesert =
                new RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect(desertCard);

        // Whenever you sacrifice a Desert, or a Desert card is put into your graveyard from your
        // hand or library, return that card to the battlefield under your control at the next end step.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.DESERT), returnDesert));
        addEffect(EffectSlot.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, returnDesert);
        addEffect(EffectSlot.ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY, returnDesert);

        PermanentHasSubtypePredicate desertPermanent = new PermanentHasSubtypePredicate(CardSubtype.DESERT);
        PermanentCount deserts = new PermanentCount(desertPermanent, CountScope.CONTROLLER);
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(5, desertPermanent),
                new CreateTokenEffect(
                        CardType.CREATURE,
                        deserts,
                        "Sand Warrior",
                        1,
                        1,
                        CardColor.RED,
                        Set.of(CardColor.RED, CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.WARRIOR),
                        Set.of(Keyword.HASTE),
                        Set.of(),
                        false,
                        false,
                        Map.of(),
                        List.of(),
                        false,
                        false,
                        false,
                        0,
                        Set.of())));
    }
}
