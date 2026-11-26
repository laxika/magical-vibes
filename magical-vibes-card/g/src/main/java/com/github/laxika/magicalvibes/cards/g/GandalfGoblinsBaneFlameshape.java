package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.Flameshape;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "96")
public class GandalfGoblinsBaneFlameshape extends Card {

    public GandalfGoblinsBaneFlameshape() {
        setBackFaceCard(new Flameshape());
        addCastingOption(new AdventureCast("{1}{R}"));

        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                BoostEffect.self(1, 1),
                new DealDamageToPlayersEffect(1, Subjects.eachOpponent()));
    }

    @Override
    public String getBackFaceClassName() {
        return "Flameshape";
    }
}
