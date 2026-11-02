package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "862")
@CardRegistration(set = "SLD", collectorNumber = "2285")
@CardRegistration(set = "MIC", collectorNumber = "2")
@CardRegistration(set = "MIC", collectorNumber = "40")
public class WilheltTheRotcleaver extends Card {

    public WilheltTheRotcleaver() {
        // Whenever another Zombie you control dies, if it didn't have decayed, create a 2/2 black
        // Zombie creature token with decayed.
        addTrigger(Triggers.creatureDies(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.DECAYED)))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                CreateTokenEffect.blackZombieWithDecayed(1));

        // At the beginning of your end step, you may sacrifice a Zombie. If you do, draw a card.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                        new DrawCardEffect(),
                        "a Zombie"),
                "Sacrifice a Zombie to draw a card?"));
    }
}
