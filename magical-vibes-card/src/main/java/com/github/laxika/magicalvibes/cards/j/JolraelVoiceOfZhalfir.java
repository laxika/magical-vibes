package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MAT", collectorNumber = "33")
public class JolraelVoiceOfZhalfir extends Card {

    public JolraelVoiceOfZhalfir() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        target(TargetFilters.landYouControl(), 0, 1)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new AnimatePermanentsEffect(
                        cardsInHand, cardsInHand,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING, Keyword.HASTE), null,
                        Set.of(CardType.CREATURE), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN,
                        new PermanentIsLandPredicate(), Set.of(CardColor.GREEN, CardColor.BLUE)
                ));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(new PermanentIsLandPredicate(), new DrawCardEffect()));
    }
}
