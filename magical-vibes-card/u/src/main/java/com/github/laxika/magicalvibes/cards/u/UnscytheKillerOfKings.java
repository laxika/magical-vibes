package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "114")
public class UnscytheKillerOfKings extends Card {

    public UnscytheKillerOfKings() {
        // Equipped creature gets +3/+3 and has first strike.
        addStaticEffect(new StaticBoostEffect(3, 3, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.attachedCreature()));

        // Whenever a creature dealt damage by equipped creature this turn dies, you may exile that
        // card. If you do, create a 2/2 black Zombie creature token.
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_ATTACHED),
                new MayEffect(
                        SequenceEffect.of(
                                new ExileTriggeringCardFromGraveyardEffect(),
                                new ConditionalEffect(HappenedThisWay.exiled(), CreateTokenEffect.blackZombie(1))),
                        "Exile that creature card and create a 2/2 black Zombie?"));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
