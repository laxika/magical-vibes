package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "24")
@CardRegistration(set = "TDC", collectorNumber = "64")
public class WillOfTheTemur extends Card {

    public WillOfTheTemur() {
        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target permanent, except it's a 4/4 Dragon creature with flying in addition to its other types",
                        new CreateTokenCopyOfTargetPermanentEffect(
                                List.of(CardSubtype.DRAGON), Set.of(CardType.CREATURE), 4, 4,
                                Map.of(), null, Set.of(Keyword.FLYING)),
                        TargetFilters.permanent()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player draws cards equal to the greatest mana value among permanents you control",
                        new DrawCardForTargetPlayerEffect(
                                new GreatestManaValueAmongControlled(new PermanentTruePredicate()), false, true),
                        anyPlayer)
        ), new ControlledCommanderAsCast()));
    }
}
