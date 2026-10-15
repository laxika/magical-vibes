package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardDealPowerDamageToSourceEffect;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "21")
public class SelflessExorcist extends Card {

    public SelflessExorcist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileTargetCreatureCardFromGraveyardDealPowerDamageToSourceEffect()),
                "{T}: Exile target creature card from a graveyard. That card deals damage equal to its power to this creature."));
    }
}
