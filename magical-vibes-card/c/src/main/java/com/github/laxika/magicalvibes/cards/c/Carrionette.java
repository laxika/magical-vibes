package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "111")
public class Carrionette extends Card {

    public Carrionette() {
        // {2}{B}{B}: Exile this card and target creature unless that creature's controller pays
        // {2}. Activate only if this card is in your graveyard.
        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{2}{B}{B}",
                List.of(UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.target()), "{2}",
                        SequenceEffect.of(new ExileTargetPermanentEffect(), new ExileSourceCardFromGraveyardEffect()))),
                "{2}{B}{B}: Exile this card and target creature unless that creature's controller "
                        + "pays {2}. Activate only if this card is in your graveyard.",
                TargetFilters.creature()
        ));
    }
}
