package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "727")
public class MisterImmortal extends Card {

    public MisterImmortal() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new ReturnSourceCardFromGraveyardToBattlefieldEffect(true)),
                "{2}{G}: Return this card from your graveyard to the battlefield tapped.",
                ActivationTimingRestriction.SORCERY_SPEED));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new ReturnSourceCardFromExileToBattlefieldEffect(true)),
                "{2}{G}: Return this card from exile to the battlefield tapped.",
                ActivationTimingRestriction.SORCERY_SPEED).withExileOnly());
    }
}
