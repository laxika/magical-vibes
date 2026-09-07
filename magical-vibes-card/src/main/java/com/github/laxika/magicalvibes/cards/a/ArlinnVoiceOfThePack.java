package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "150")
public class ArlinnVoiceOfThePack extends Card {

    public ArlinnVoiceOfThePack() {
        PermanentPredicate wolfOrWerewolf = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WOLF, CardSubtype.WEREWOLF))
        ));
        addEffect(EffectSlot.STATIC, new ControlledPermanentsEnterWithAdditionalCountersEffect(
                wolfOrWerewolf, 1));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        "Wolf", 2, 2, CardColor.GREEN,
                        List.of(CardSubtype.WOLF), Set.of(), Set.of()
                )),
                "\u22122: Create a 2/2 green Wolf creature token."
        ));
    }
}
