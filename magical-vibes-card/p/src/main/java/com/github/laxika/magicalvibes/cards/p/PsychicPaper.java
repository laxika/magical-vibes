package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AsThisBecomesAttachedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetChosenNameAndCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "181")
@CardRegistration(set = "WHO", collectorNumber = "786")
public class PsychicPaper extends Card {

    public PsychicPaper() {
        addStaticEffect(new AsThisBecomesAttachedEffect(SequenceEffect.of(
                new ChooseCardNameAtResolutionEffect(CardType.CREATURE),
                new ChooseSubtypeForSourceEffect())));

        addStaticEffect(new SetChosenNameAndCreatureTypeEffect(Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.WARD, Subjects.attachedCreature()));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                        UnlessPaysEffect.counterUnlessPays(1)), Subjects.attachedCreature()));
        addStaticEffect(new CantBeBlockedEffect());

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
