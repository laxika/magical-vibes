package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "92")
@CardRegistration(set = "VOC", collectorNumber = "127")
@CardRegistration(set = "C17", collectorNumber = "109")
public class DarkImpostor extends Card {

    public DarkImpostor() {
        String exileAbilityLink = "printed-exile";
        // {4}{B}{B}: Exile target creature and put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}{B}",
                List.of(
                        new ExileTargetPermanentAndTrackWithSourceEffect(exileAbilityLink),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "{4}{B}{B}: Exile target creature and put a +1/+1 counter on this creature.",
                TargetFilters.creature()
        ));

        // Dark Impostor has all activated abilities of all creature cards exiled with it.
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfExiledCardsEffect(
                false, new CardTypePredicate(CardType.CREATURE), exileAbilityLink));
    }
}
