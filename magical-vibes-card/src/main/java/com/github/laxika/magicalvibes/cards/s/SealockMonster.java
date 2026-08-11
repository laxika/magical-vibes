package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "62")
public class SealockMonster extends Card {

    public SealockMonster() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                "an Island"));

        SourceIsMonstrous monstrous = new SourceIsMonstrous();
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}{U}",
                List.of(new MonstrosityEffect(3)),
                "{5}{U}{U}: Monstrosity 3."
        ).withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));

        target(TargetFilters.land()).addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS,
                new GrantBasicLandTypeToTargetEffect(EffectDuration.CONTINUOUS, CardSubtype.ISLAND));
    }
}
