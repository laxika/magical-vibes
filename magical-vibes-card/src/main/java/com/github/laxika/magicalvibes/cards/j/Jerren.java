package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.OrmendahlTheCorrupter;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "109")
public class Jerren extends Card {

    private static final CreateTokenEffect CREATE_HUMAN = new CreateTokenEffect(
            "Human", 1, 1, CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of());
    private static final SequenceEffect DEATH_REWARD = SequenceEffect.of(
            new LoseLifeEffect(1), CREATE_HUMAN);

    public Jerren() {
        setBackFaceCard(new OrmendahlTheCorrupter());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, DEATH_REWARD);
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.HUMAN), DEATH_REWARD));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET)),
                "{2}: Target Human you control gains lifelink until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                                new PermanentControlledBySourceControllerPredicate())),
                        "Target must be a Human you control")));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new AllOf(List.of(new ControllerLifeAtLeast(13), new ControllerLifeAtMost(13))),
                        new MayPayManaEffect("{4}{B}{B}", new TransformSelfEffect(),
                                "Pay {4}{B}{B} to transform Jerren?")));
    }

    @Override
    public String getBackFaceClassName() {
        return "OrmendahlTheCorrupter";
    }
}
