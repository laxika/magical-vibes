package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "32")
public class TeyoTheShieldmage extends Card {

    public TeyoTheShieldmage() {
        addEffect(EffectSlot.STATIC, new GrantControllerKeywordEffect(Keyword.HEXPROOF));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        "Wall", 0, 3, CardColor.WHITE, List.of(CardSubtype.WALL),
                        Set.of(Keyword.DEFENDER), Set.of()
                )),
                "−2: Create a 0/3 white Wall creature token with defender."
        ));
    }
}
