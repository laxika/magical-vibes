package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;

@CardRegistration(set = "LTC", collectorNumber = "56")
@CardRegistration(set = "LTC", collectorNumber = "139")
@CardRegistration(set = "LTC", collectorNumber = "460")
public class ForthEorlingas extends Card {

    public ForthEorlingas() {
        addSpellEffect(new CreateTokenEffect(
                new XValue(), "Human Knight", 2, 2, CardColor.RED,
                List.of(CardSubtype.HUMAN, CardSubtype.KNIGHT),
                Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of()));
        addSpellEffect(new RegisterGlobalTriggeredAbilityEffect(new TriggeredAbility(
                Triggers.creatureYouControlDealsCombatDamageToPlayer(), TriggerOptions.ONE_OR_MORE_PLAYERS,
                new BecomeMonarchEffect())));
    }
}
