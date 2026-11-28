package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromEverythingUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "11")
@CardRegistration(set = "BLC", collectorNumber = "47")
public class PerchProtection extends Card {

    public PerchProtection() {
        addStaticEffect(new GiftEffect());
        // The gift opponent is chosen, not targeted (CR 702.174a); with two players it is the opponent.
        addSpellEffect(new ConditionalEffect(new GiftPromised(), new ExtraTurnEffect(1, Subjects.eachOpponent())));
        addSpellEffect(new CreateTokenEffect(4, "Bird", 2, 2,
                CardColor.BLUE, List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));
        addSpellEffect(new ConditionalEffect(new GiftPromised(),
                new PhaseOutEffect(Subjects.controlledBy(Subjects.you(), new PermanentTruePredicate()))));
        addSpellEffect(new ConditionalEffect(new GiftPromised(),
                GrantStaticEffectToPlayerEffect.untilYourNextTurn(new LifeTotalCantChangeEffect())));
        addSpellEffect(new ConditionalEffect(new GiftPromised(),
                new GrantProtectionFromEverythingUntilNextTurnEffect()));
        addSpellEffect(new ExileSpellEffect());
    }
}
