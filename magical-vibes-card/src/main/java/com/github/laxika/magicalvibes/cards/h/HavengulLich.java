package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "139")
public class HavengulLich extends Card {

    public HavengulLich() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect()),
                "{1}: You may cast target creature card in a graveyard this turn. When you cast it this turn, this creature gains all activated abilities of that card until end of turn."
        ));
    }
}
