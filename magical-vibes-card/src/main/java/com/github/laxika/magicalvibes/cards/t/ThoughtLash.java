package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileControllerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "39")
public class ThoughtLash extends Card {

    public ThoughtLash() {
        // The library-exile ability triggers separately when the cumulative upkeep is not paid.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                CumulativeUpkeepEffect.exileTopCard(List.of(new QueueReflexiveAbilityEffect(
                        new ExileControllerLibraryEffect()))));

        // Exile the top card of your library: Prevent the next 1 damage that would be dealt to you
        // this turn.
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new ExileTopCardOfLibraryCost(1), PreventDamageEffect.nextToController(1)),
                "Exile the top card of your library: Prevent the next 1 damage that would be dealt to you this turn."));
    }
}
