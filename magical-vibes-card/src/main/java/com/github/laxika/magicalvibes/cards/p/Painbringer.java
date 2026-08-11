package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "155")
public class Painbringer extends Card {

    public Painbringer() {
        // {T}, Exile any number of cards from your graveyard: Target creature gets -X/-X until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileXCardsFromGraveyardCost(),
                        new BoostTargetCreatureEffect(new Scaled(new XValue(), -1), new Scaled(new XValue(), -1))
                ),
                "{T}, Exile any number of cards from your graveyard: Target creature gets -X/-X until end of turn.",
                TargetFilters.creature()
        ));
    }
}
