package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "99")
public class NecrogenCommunion extends Card {

    public NecrogenCommunion() {
        target(TargetFilters.creatureYouControl())
                .addStaticEffect(new GrantKeywordEffect(Keyword.TOXIC, Subjects.attachedCreature()))
                .addStaticEffect(new GrantEffectEffect(new ToxicEffect(2), Subjects.attachedCreature()))
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(true));
    }
}
