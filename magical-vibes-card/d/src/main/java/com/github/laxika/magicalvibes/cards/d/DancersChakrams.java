package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "17")
@CardRegistration(set = "FIC", collectorNumber = "105")
public class DancersChakrams extends Card {

    public DancersChakrams() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new LivingWeaponEffect(new CreateTokenEffect("Hero", 1, 1, null,
                        List.of(CardSubtype.HERO), Set.of(), Set.of())));

        addStaticEffect(new StaticBoostEffect(2, 2, Set.of(Keyword.LIFELINK), Subjects.attachedCreature()));
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.PERFORMER, Subjects.attachedCreature()));
        addStaticEffect(new GrantEffectEffect(new StaticBoostEffect(2, 2, Set.of(Keyword.LIFELINK),
                        Subjects.permanentsYouControl(), new PermanentAllOfPredicate(List.of(
                                new PermanentIsCommanderPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))),
                        Subjects.attachedCreature()));

        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
