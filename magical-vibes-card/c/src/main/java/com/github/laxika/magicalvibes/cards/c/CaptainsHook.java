package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "177")
public class CaptainsHook extends Card {

    public CaptainsHook() {
        addStaticEffect(new StaticBoostEffect(2, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.MENACE, Subjects.attachedCreature()));
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.PIRATE, Subjects.attachedCreature()));
        addTrigger(Triggers.becomesUnattachedFromPermanent(),
                new DestroyTargetPermanentEffect(Subjects.triggering()));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
