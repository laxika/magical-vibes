package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "54")
public class AbyssalHarvester extends Card {

    public AbyssalHarvester() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        false,
                        List.of(CardSubtype.NIGHTMARE),
                        false,
                        false,
                        null,
                        null,
                        null,
                        Set.of(),
                        false,
                        true,
                        CardSubtype.NIGHTMARE)),
                "{T}: Exile target creature card from a graveyard that was put there this turn. "
                        + "Create a token that's a copy of it, except it's a Nightmare in addition to its other types. "
                        + "Then exile all other Nightmare tokens you control."));
    }
}
