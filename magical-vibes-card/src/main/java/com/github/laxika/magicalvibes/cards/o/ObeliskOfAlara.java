package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "140")
public class ObeliskOfAlara extends Card {

    public ObeliskOfAlara() {
        // {1}{W}, {T}: You gain 5 life.
        addActivatedAbility(new ActivatedAbility(true, "{1}{W}",
                List.of(new GainLifeEffect(5)),
                "{1}{W}, {T}: You gain 5 life."));

        // {1}{U}, {T}: Draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(true, "{1}{U}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{1}{U}, {T}: Draw a card, then discard a card."));

        // {1}{B}, {T}: Target creature gets -2/-2 until end of turn.
        addActivatedAbility(new ActivatedAbility(true, "{1}{B}",
                List.of(new BoostTargetCreatureEffect(-2, -2)),
                "{1}{B}, {T}: Target creature gets -2/-2 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));

        // {1}{R}, {T}: This artifact deals 3 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(true, "{1}{R}",
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(3)),
                "{1}{R}, {T}: This artifact deals 3 damage to target player or planeswalker."));

        // {1}{G}, {T}: Target creature gets +4/+4 until end of turn.
        addActivatedAbility(new ActivatedAbility(true, "{1}{G}",
                List.of(new BoostTargetCreatureEffect(4, 4)),
                "{1}{G}, {T}: Target creature gets +4/+4 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));
    }
}
