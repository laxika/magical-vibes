package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "225")
public class NarsetTranscendent extends Card {

    public NarsetTranscendent() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                        false)),
                "+1: Look at the top card of your library. If it's a noncreature, nonland card, you may reveal it and put it into your hand."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new RegisterDelayedControllerSpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        new StackEntryCastFromZonePredicate(Zone.HAND),
                        List.of(new GrantKeywordsToCastSpellEffect(Set.of(Keyword.REBOUND))),
                        true,
                        false,
                        null)),
                "−2: When you next cast an instant or sorcery spell from your hand this turn, it gains rebound."
        ));

        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(new CreateEmblemEffect(
                        List.of(new OpponentsCantCastSpellsMatchingPredicateEffect(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))),
                        "Your opponents can't cast noncreature spells.")),
                "−9: You get an emblem with \"Your opponents can't cast noncreature spells.\""
        ));
    }
}
