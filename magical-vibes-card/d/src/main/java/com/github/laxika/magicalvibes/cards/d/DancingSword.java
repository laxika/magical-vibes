package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.LoseAllLandTypesEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "8")
public class DancingSword extends Card {

    public DancingSword() {
        addStaticEffect(new StaticBoostEffect(2, 1, Subjects.attachedCreature()));

        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED), new MayEffect(
                SequenceEffect.of(new AnimatePermanentsEffect(new Fixed(2), new Fixed(1), List.of(CardSubtype.CONSTRUCT),
                                Set.of(Keyword.FLYING), null, Set.of(CardType.ARTIFACT), Subjects.source(),
                                Duration.PERMANENT, null),
                        LoseAllLandTypesEffect.subtypes(Set.of(CardSubtype.EQUIPMENT), Subjects.source(),
                                Duration.PERMANENT),
                        new GrantStaticEffectToSourceEffect(new GrantTriggeredAbilityEffect(
                                new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                                UnlessPaysEffect.counterUnlessPays(1)), Subjects.source()))),
                "Have Dancing Sword become a creature?"));

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
