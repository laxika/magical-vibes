package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "91")
@CardRegistration(set = "ME1", collectorNumber = "119")
public class GargantuanGorilla extends Card {

    public GargantuanGorilla() {
        // At the beginning of your upkeep, you may sacrifice a Forest. If you sacrifice a snow Forest
        // this way, this creature gains trample until end of turn. If you don't sacrifice a Forest,
        // sacrifice this creature and it deals 7 damage to you.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                                Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.toA(StepKind.SACRIFICE,
                                new PermanentHasSupertypePredicate(CardSupertype.SNOW)),
                                new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.source(), Duration.UNTIL_END_OF_TURN))),
                "Sacrifice a Forest? If you don't, sacrifice this creature and it deals 7 damage to you.",
                SequenceEffect.of(new SacrificeSelfEffect(), DealDamageEffect.to(Subjects.you(), 7))));

        // {T}: This creature deals damage equal to its power to another target creature. That
        // creature deals damage equal to its power to this creature. (fight mechanic)
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new SourceFightsTargetCreatureEffect(false)),
                "{T}: This creature deals damage equal to its power to another target creature. "
                        + "That creature deals damage equal to its power to this creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        "Target must be another creature")
        ));
    }
}
