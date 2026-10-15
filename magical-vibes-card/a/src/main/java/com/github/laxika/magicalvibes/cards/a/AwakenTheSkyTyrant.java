package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C15", collectorNumber = "24")
public class AwakenTheSkyTyrant extends Card {

    public AwakenTheSkyTyrant() {
        addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT,
                new SacrificeSelfThenEffect(new CreateTokenEffect(
                        "Dragon", 5, 5, CardColor.RED, List.of(CardSubtype.DRAGON),
                        Set.of(Keyword.FLYING), Set.of())));
    }
}
