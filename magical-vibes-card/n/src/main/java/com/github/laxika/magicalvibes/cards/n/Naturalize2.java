package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "341")
@CardRegistration(set = "MB2", collectorNumber = "579")
public class Naturalize2 extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("Naturalize2", new OracleData(
                "Naturalize 2",
                CardType.INSTANT,
                Set.of(),
                "{1}{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(),
                "Destroy target artifact, enchantment, emblem, or gameplay tracker. "
                        + "(Trackers include play aids such as dungeons, city's blessing, and monarch. "
                        + "When you destroy it, the associated object or designation is removed from the game.)",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public Naturalize2() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Target must be an artifact or enchantment"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
