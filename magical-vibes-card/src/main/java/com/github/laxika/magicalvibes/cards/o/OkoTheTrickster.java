package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "309")
public class OkoTheTrickster extends Card {

    public OkoTheTrickster() {
        TargetFilter creatureYouControl = TargetFilters.creatureYouControl();
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "+1: Put two +1/+1 counters on up to one target creature you control.",
                null,
                +1,
                null,
                null,
                List.of(creatureYouControl),
                0,
                1));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(
                        new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(),
                        PreventDamageEffect.allToSelf()),
                "0: Until end of turn, Oko becomes a copy of target creature you control. "
                        + "Prevent all damage that would be dealt to him this turn.",
                creatureYouControl));

        PermanentPredicate creatureYouControlPredicate = new PermanentAllOfPredicate(
                List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()));
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(
                        new SetBasePowerToughnessEffect(
                                10,
                                10,
                                GrantScope.ALL_CREATURES,
                                EffectDuration.UNTIL_END_OF_TURN,
                                creatureYouControlPredicate),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES)),
                "\u22127: Until end of turn, each creature you control has base power and toughness 10/10 "
                        + "and gains trample."));
    }
}
