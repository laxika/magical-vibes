package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Back face of {@link BrineComber}. */
public class BrineboundGift extends Card {

    public BrineboundGift() {
        target(TargetFilters.creature());

        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.whiteSpirit(1));
        addTrigger(Triggers.becomesTargetOfAuraSpell(), CreateTokenEffect.whiteSpirit(1));
        addStaticEffect(new ExileInsteadOfGraveyardReplacementEffect());
    }
}
