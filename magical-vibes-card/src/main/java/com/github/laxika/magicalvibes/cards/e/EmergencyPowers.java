package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerMainPhase;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "169")
public class EmergencyPowers extends Card {

    public EmergencyPowers() {
        addEffect(EffectSlot.SPELL, new EachPlayerShufflesZonesIntoLibraryEffect());
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(7));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControllerMainPhase(),
                new MayEffect(
                        new PutUpToCardsFromHandOntoBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardIsPermanentPredicate(),
                                        new CardMaxManaValuePredicate(7)
                                )),
                                "permanent card with mana value 7 or less",
                                1
                        ),
                        "Put a permanent card with mana value 7 or less from your hand onto the battlefield?"
                )
        ));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
