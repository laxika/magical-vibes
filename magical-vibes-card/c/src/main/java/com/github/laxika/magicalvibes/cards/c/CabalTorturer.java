package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "53")
public class CabalTorturer extends Card {

    public CabalTorturer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new BoostTargetCreatureEffect(-1, -1)),
                "{B}, {T}: Target creature gets -1/-1 until end of turn.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{B}{B}",
                List.of(new BoostTargetCreatureEffect(-2, -2)),
                "{3}{B}{B}, {T}: Target creature gets -2/-2 until end of turn. Activate only if there are seven or more cards in your graveyard.",
                TargetFilters.creature()
        ).withRequiredGraveyardCards(new CardTruePredicate(), 7, "cards in your graveyard"));
    }
}
