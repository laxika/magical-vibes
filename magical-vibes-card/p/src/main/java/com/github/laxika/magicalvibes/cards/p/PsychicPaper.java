package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetChosenNameAndCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "181")
@CardRegistration(set = "WHO", collectorNumber = "786")
public class PsychicPaper extends Card {

    public PsychicPaper() {
        addTrigger(Triggers.becomesAttachedToCreature(),
                SequenceEffect.of(
                        new ChooseCardNameAtResolutionEffect(CardType.CREATURE),
                        new ChooseSubtypeForSourceEffect()));

        addStaticEffect(new SetChosenNameAndCreatureTypeEffect(GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantKeywordEffect(Keyword.WARD, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                        new CounterUnlessPaysEffect(1)), GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new CantBeBlockedEffect());

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
