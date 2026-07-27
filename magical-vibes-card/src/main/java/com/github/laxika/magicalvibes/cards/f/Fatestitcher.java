package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "43")
public class Fatestitcher extends Card {

    public Fatestitcher() {
        // {T}: You may tap or untap another target permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TapOrUntapTargetPermanentEffect()),
                "{T}: You may tap or untap another target permanent.",
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        "Target must be another permanent"
                )
        ));

        // Unearth {U}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{U}");
    }
}
