package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "77")
public class ScarbladeElite extends Card {

    public ScarbladeElite() {
        // {T}, Exile an Assassin card from your graveyard: Destroy target creature.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new ExileCardFromGraveyardCost(CardSubtype.ASSASSIN), new DestroyTargetPermanentEffect()),
                "{T}, Exile an Assassin card from your graveyard: Destroy target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
