package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "63")
public class GrotesqueHybrid extends Card {

    public GrotesqueHybrid() {
        addTrigger(Triggers.dealsDamageToCreature().withCombatDamageOnly(),
                new DestroyTargetPermanentEffect(Subjects.triggering(), true));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new GrantKeywordEffect(Keyword.FLYING, Subjects.source()),
                        new GrantProtectionFromColorUntilEndOfTurnEffect(CardColor.GREEN, Subjects.source()),
                        new GrantProtectionFromColorUntilEndOfTurnEffect(CardColor.WHITE, Subjects.source())
                ),
                "Discard a card: This creature gains flying and protection from green and from white until end of turn."
        ));
    }
}
