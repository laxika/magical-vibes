package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "291")
public class CopperGnomes extends Card {

    public CopperGnomes() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new SacrificeSelfCost(),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.ARTIFACT), "artifact"),
                                "Put an artifact card from your hand onto the battlefield?"
                        )
                ),
                "{4}, Sacrifice this creature: You may put an artifact card from your hand onto the battlefield."
        ));
    }
}
