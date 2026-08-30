package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BandsWithOtherEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LEG", collectorNumber = "194")
public class MasterOfTheHunt extends Card {

    public MasterOfTheHunt() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{G}",
                List.of(new CreateTokenEffect(
                        1,
                        "Wolves of the Hunt",
                        1,
                        1,
                        CardColor.GREEN,
                        List.of(CardSubtype.WOLF),
                        Set.of(),
                        Set.of(),
                        Map.of(EffectSlot.STATIC, new BandsWithOtherEffect("Wolves of the Hunt")))),
                "{2}{G}{G}: Create a 1/1 green Wolf creature token named Wolves of the Hunt. It has bands with other creatures named Wolves of the Hunt."
        ));
    }
}
