package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "202")
public class DimirDoppelganger extends Card {

    public DimirDoppelganger() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}{B}",
                List.of(new ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect()),
                "Exile target creature card from a graveyard. This creature becomes a copy of that card, except it has this ability."));
    }
}
