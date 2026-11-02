package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "174")
public class SquirrelSanctuary extends Card {

    public SquirrelSanctuary() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect("Squirrel", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SQUIRREL), Set.of(), Set.of()));

        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL),
                new MayPayManaEffect("{1}", ReturnToHandEffect.self(),
                        "Pay {1} to return Squirrel Sanctuary to its owner's hand?"));
    }
}
