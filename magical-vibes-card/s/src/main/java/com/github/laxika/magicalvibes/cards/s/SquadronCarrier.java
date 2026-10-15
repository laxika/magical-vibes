package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "2")
public class SquadronCarrier extends Card {

    public SquadronCarrier() {
        ActivatedAbility conjurePilot = new ActivatedAbility(
                false,
                "{W}",
                List.of(new CreateTokenCopyOfCardEffect(
                        new StarfighterPilot(),
                        new CreateTokenCopyOfTargetPermanentEffect())),
                "Exhaust — {W}: Conjure a card named Starfighter Pilot onto the battlefield. "
                        + "(Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust();
        addActivatedAbility(conjurePilot);

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                conjurePilot,
                GrantScope.OWN_PERMANENTS,
                new PermanentHasSubtypePredicate(CardSubtype.SPACECRAFT)));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(10, CounterType.CHARGE),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES)));
    }
}
