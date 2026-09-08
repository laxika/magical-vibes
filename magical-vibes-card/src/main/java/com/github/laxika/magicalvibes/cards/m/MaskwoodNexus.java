package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "240")
public class MaskwoodNexus extends Card {

    public MaskwoodNexus() {
        addEffect(EffectSlot.STATIC, new GrantAllCreatureTypesToOwnCreaturesEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new CreateTokenEffect(
                        "Shapeshifter", 2, 2, CardColor.BLUE,
                        List.of(CardSubtype.SHAPESHIFTER), Set.of(Keyword.CHANGELING), Set.of())),
                "{3}, {T}: Create a 2/2 blue Shapeshifter creature token with changeling."
        ));
    }
}
