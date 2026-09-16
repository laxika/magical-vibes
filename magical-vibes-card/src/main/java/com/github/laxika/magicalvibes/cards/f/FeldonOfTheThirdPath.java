package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSR", collectorNumber = "344")
public class FeldonOfTheThirdPath extends Card {

    public FeldonOfTheThirdPath() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        true,
                        List.of(),
                        true,
                        false,
                        Set.of(CardType.ARTIFACT),
                        true)),
                "{2}{R}, {T}: Create a token that's a copy of target creature card in your graveyard, "
                        + "except it's an artifact in addition to its other types. It gains haste. "
                        + "Sacrifice it at the beginning of the next end step."));
    }
}
