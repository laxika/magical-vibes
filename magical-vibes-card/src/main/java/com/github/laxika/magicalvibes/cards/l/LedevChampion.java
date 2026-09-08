package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesBoostSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "186")
public class LedevChampion extends Card {

    public LedevChampion() {
        addEffect(EffectSlot.ON_ATTACK, new TapCreaturesBoostSelfEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{W}",
                List.of(new CreateTokenEffect(
                        1, "Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.SOLDIER), Set.of(Keyword.LIFELINK), Set.of())),
                "{3}{G}{W}: Create a 1/1 white Soldier creature token with lifelink."
        ));
    }
}
