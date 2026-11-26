package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MSC", collectorNumber = "727")
public class MisterImmortal extends Card {

    public MisterImmortal() {
        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new PutCardsOntoBattlefieldEffect(Subjects.sourceObjectCard()).enteringTapped()),
                "{2}{G}: Return this card from your graveyard to the battlefield tapped."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new ReturnSourceCardFromExileToBattlefieldEffect(true)),
                "{2}{G}: Return this card from exile to the battlefield tapped.").withZones(AbilityZones.EXILE));
    }
}
