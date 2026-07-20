package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GideonOfTheTrialsEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AKH", collectorNumber = "14")
public class GideonOfTheTrials extends Card {

    public GideonOfTheTrials() {
        // +1: Until your next turn, prevent all damage target permanent would deal.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(PreventDamageEffect.allByTargetPermanentUntilNextTurn()),
                "+1: Until your next turn, prevent all damage target permanent would deal.",
                new PermanentPredicateTargetFilter(new PermanentTruePredicate(), "Target must be a permanent")
        ));

        // 0: Until end of turn, Gideon of the Trials becomes a 4/4 Human Soldier creature with
        // indestructible that's still a planeswalker. Prevent all damage that would be dealt to him this turn.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(
                        new AnimatePermanentsEffect(4, 4, List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                                Set.of(Keyword.INDESTRUCTIBLE)),
                        PreventDamageEffect.allToSelf()
                ),
                "0: Until end of turn, Gideon of the Trials becomes a 4/4 Human Soldier creature with indestructible that's still a planeswalker. Prevent all damage that would be dealt to him this turn."
        ));

        // 0: You get an emblem with "As long as you control a Gideon planeswalker, you can't lose the
        // game and your opponents can't win the game."
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new GideonOfTheTrialsEmblemEffect()),
                "0: You get an emblem with \"As long as you control a Gideon planeswalker, you can't lose the game and your opponents can't win the game.\""
        ));
    }
}
