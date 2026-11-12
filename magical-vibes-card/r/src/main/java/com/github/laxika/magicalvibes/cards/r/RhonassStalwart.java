package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "133")
@CardRegistration(set = "AKR", collectorNumber = "214")
public class RhonassStalwart extends Card {

    public RhonassStalwart() {
        // Exert: "You may exert this creature as it attacks. When you do, it gets +1/+1 until end of
        // turn and can't be blocked by creatures with power 2 or less this turn." Modeled as an
        // optional attack trigger (matching Hooded Brawler / Khenra Scrapper). "Can't be blocked by
        // power 2 or less" ≡ "can be blocked only by power 3 or greater" (Steel Leaf Champion).
        // Choosing to exert also keeps the creature tapped through its next untap step.
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        SkipNextUntapEffect.exert(),
                        new QueueReflexiveAbilityEffect(SequenceEffect.of(
                                new BoostSelfEffect(1, 1),
                                new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                                new PermanentPowerAtLeastPredicate(3),
                                "creatures with power 3 or greater",
                                true)))
                ),
                "Exert Rhonas's Stalwart as it attacks? (It gets +1/+1 until end of turn and can't be blocked by creatures with power 2 or less this turn.)"
        ));
    }
}
