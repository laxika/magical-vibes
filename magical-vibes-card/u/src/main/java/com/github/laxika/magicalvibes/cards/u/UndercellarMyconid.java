package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "226")
public class UndercellarMyconid extends Card {

    public UndercellarMyconid() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, saprolingToken());
        addEffect(EffectSlot.ON_DEATH, saprolingToken());
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }

    private static CreateTokenEffect saprolingToken() {
        return new CreateTokenEffect(
                "Saproling",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.SAPROLING),
                Set.of(),
                Set.of()
        );
    }
}
