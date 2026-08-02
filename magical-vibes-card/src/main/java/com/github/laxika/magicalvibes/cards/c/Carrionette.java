package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSourceFromGraveyardAndTargetCreatureUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "111")
public class Carrionette extends Card {

    public Carrionette() {
        // {2}{B}{B}: Exile this card and target creature unless that creature's controller pays
        // {2}. Activate only if this card is in your graveyard.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{B}",
                List.of(new ExileSourceFromGraveyardAndTargetCreatureUnlessPaysEffect("{2}")),
                "{2}{B}{B}: Exile this card and target creature unless that creature's controller "
                        + "pays {2}. Activate only if this card is in your graveyard.",
                TargetFilters.creature()
        ));
    }
}
