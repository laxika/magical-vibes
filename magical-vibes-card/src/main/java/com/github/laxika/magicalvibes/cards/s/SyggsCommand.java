package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "244")
@CardRegistration(set = "ECL", collectorNumber = "342")
public class SyggsCommand extends Card {

    public SyggsCommand() {
        setAllowSharedTargets(true);

        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");
        var merfolkYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.MERFOLK),
                "Target must be a Merfolk you control.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target Merfolk you control",
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        merfolkYouControl),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures target player controls gain lifelink until end of turn",
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET_PLAYERS_CREATURES),
                        anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player draws a card",
                        new DrawCardForTargetPlayerEffect(1, false, true),
                        anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Tap target creature. Put a stun counter on it",
                        List.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                new PutCounterOnTargetPermanentEffect(CounterType.STUN)),
                        TargetFilters.creature())
        ), 2));
    }
}
