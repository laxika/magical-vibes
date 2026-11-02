package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "226")
public class UndercellarMyconid extends Card {

    public UndercellarMyconid() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), saprolingToken());
        addTrigger(Triggers.dies(ObjectRelation.THIS), saprolingToken());
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
