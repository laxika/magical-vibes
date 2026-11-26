package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "132")
public class HawkeyesBow extends Card {

    public HawkeyesBow() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.REACH, Subjects.attachedCreature()));

        addTrigger(List.of(
                        Triggers.permanentYouControlBecomesTapped()
                                .withFilter(new PermanentIsHostOfSourceAuraPredicate()),
                        Triggers.permanentOpponentControlsBecomesTapped()
                                .withFilter(new PermanentIsHostOfSourceAuraPredicate())),
                DealDamageEffect.to(Subjects.eachOpponent(), 1).byTriggeringPermanent());

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
