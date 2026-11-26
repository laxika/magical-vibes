package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "293")
public class Sarcatog extends Card {

    public Sarcatog() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileNCardsFromGraveyardCost(2, null), BoostEffect.self(1, 1)),
                "Exile two cards from your graveyard: This creature gets +1/+1 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        BoostEffect.self(1, 1)),
                "Sacrifice an artifact: This creature gets +1/+1 until end of turn."
        ));
    }
}
