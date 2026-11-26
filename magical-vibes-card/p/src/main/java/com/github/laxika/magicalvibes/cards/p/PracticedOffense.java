package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "25")
@CardRegistration(set = "SOS", collectorNumber = "313")
public class PracticedOffense extends Card {

    public PracticedOffense() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ))
                .addSpellEffect(PutCountersEffect.onEachTargetPlayerControls(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));

        target(TargetFilters.creature())
                .addSpellEffect(new GrantChosenKeywordEffect(
                        List.of(Keyword.DOUBLE_STRIKE, Keyword.LIFELINK), Subjects.target()));

        addCastingOption(new FlashbackCast("{1}{W}"));
    }
}
