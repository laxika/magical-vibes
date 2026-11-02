package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MAT", collectorNumber = "33")
@CardRegistration(set = "MAT", collectorNumber = "83")
@CardRegistration(set = "MAT", collectorNumber = "133")
@CardRegistration(set = "MAT", collectorNumber = "168")
@CardRegistration(set = "MAT", collectorNumber = "230")
public class JolraelVoiceOfZhalfir extends Card {

    public JolraelVoiceOfZhalfir() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        target(TargetFilters.landYouControl(), 0, 1)
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new AnimatePermanentsEffect(
                        cardsInHand, cardsInHand,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING, Keyword.HASTE), null,
                        Set.of(CardType.CREATURE), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN,
                        new PermanentIsLandPredicate(), Set.of(CardColor.GREEN, CardColor.BLUE)
                ));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(new PermanentIsLandPredicate(), new DrawCardEffect()));
    }
}
