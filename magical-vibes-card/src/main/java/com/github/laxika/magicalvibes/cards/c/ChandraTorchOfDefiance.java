package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayCastOrDealDamageEffect;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "110")
public class ChandraTorchOfDefiance extends Card {

    public ChandraTorchOfDefiance() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new ExileTopCardMayCastOrDealDamageEffect(2)),
                "+1: Exile the top card of your library. You may cast that card. If you don't, Chandra deals 2 damage to each opponent."
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new AwardManaEffect(ManaColor.RED, 2)),
                "+1: Add {R}{R}."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DealDamageToTargetCreatureEffect(4)),
                "−3: Chandra deals 4 damage to target creature."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new DealDamageToAnyTargetOnControllerSpellCastEffect(5)),
                        "Whenever you cast a spell, this emblem deals 5 damage to any target."
                )),
                "−7: You get an emblem with \"Whenever you cast a spell, this emblem deals 5 damage to any target.\""
        ));
    }
}
