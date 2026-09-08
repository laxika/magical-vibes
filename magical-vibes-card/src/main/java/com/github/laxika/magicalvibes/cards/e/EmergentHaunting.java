package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "46")
public class EmergentHaunting extends Card {

    public EmergentHaunting() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(
                        new NotCondition(new ControllerCastSpellThisTurn(new CardTruePredicate(), true)),
                        new NotCondition(new SourceIsCreature()))),
                new AnimatePermanentsEffect(
                        3, 3,
                        List.of(CardSubtype.SPIRIT),
                        Set.of(Keyword.FLYING),
                        null,
                        Set.of(CardType.CREATURE),
                        GrantScope.SELF,
                        EffectDuration.PERMANENT
                )));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new SurveilEffect(1)),
                "{2}{U}: Surveil 1."
        ));
    }
}
