package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CreaturesDiedThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "205")
public class LagomosHandOfHatred extends Card {

    public LagomosHandOfHatred() {
        // At the beginning of combat on your turn, create a 2/1 red Elemental creature token with
        // trample and haste. Sacrifice it at the beginning of the next end step.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new CreateTokenEffect(
                "Elemental", 2, 1, CardColor.RED, List.of(CardSubtype.ELEMENTAL),
                Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of()));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new SacrificeCreatedPermanentsAtEndStepEffect());

        // {T}: Search your library for a card, put it into your hand, then shuffle. Activate only
        // if five or more creatures died this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SearchLibraryEffect()),
                "{T}: Search your library for a card, put it into your hand, then shuffle. Activate only if five or more creatures died this turn."
        ).withActivationCondition(
                new CreaturesDiedThisTurnAtLeast(5),
                "Activate only if five or more creatures died this turn."
        ));
    }
}
