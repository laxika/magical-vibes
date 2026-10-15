package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantExtraLoyaltyActivationToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD6Effect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "79")
public class CometStellarPup extends Card {

    public CometStellarPup() {
        CardEffect squirrels = SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.LOYALTY, 2),
                new CreateTokenEffect(2, "Squirrel", 1, 1, CardColor.GREEN,
                        Set.of(CardColor.GREEN), List.of(CardSubtype.SQUIRREL), Set.of(), Set.of(Keyword.HASTE)));

        CardEffect returnCard = SequenceEffect.of(
                new RemoveCounterFromSourceEffect(CounterType.LOYALTY, 1),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardMaxManaValuePredicate(2))
                        .mandatory(true)
                        .build());

        CardEffect damage = SequenceEffect.of(
                DealDamageToTargetCreatureOrPlayerEffect.chooseDuringResolution(new CountersOnSource(CounterType.LOYALTY)),
                new RemoveCounterFromSourceEffect(CounterType.LOYALTY, 2));

        CardEffect extraActivations = SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.LOYALTY, 1),
                new GrantExtraLoyaltyActivationToSourceEffect(),
                new GrantExtraLoyaltyActivationToSourceEffect());

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new RollD6Effect(List.of(
                        squirrels,
                        squirrels,
                        returnCard,
                        damage,
                        damage,
                        extraActivations))),
                "0: Roll a six-sided die. 1 or 2 — [+2], then create two 1/1 green Squirrel "
                        + "creature tokens. They gain haste until end of turn. 3 — [−1], then "
                        + "return a card with mana value 2 or less from your graveyard to your hand. "
                        + "4 or 5 — Comet deals damage equal to the number of loyalty counters on "
                        + "him to a creature or player, then [−2]. 6 — [+1], and you may activate "
                        + "Comet's loyalty ability two more times this turn."));
    }
}
