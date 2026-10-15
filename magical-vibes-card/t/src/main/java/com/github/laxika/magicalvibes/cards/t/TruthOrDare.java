package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayWithTargetPlayerHandRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "32")
public class TruthOrDare extends Card {

    public TruthOrDare() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent."
        )).addEffect(EffectSlot.SPELL, TargetPlayerChoosesOneEffect.forTargetedPlayer(List.of(
                new ChooseOneEffect.ChooseOneOption("Truth", new PlayWithTargetPlayerHandRevealedEffect()),
                new ChooseOneEffect.ChooseOneOption("Dare", new MillEffect(
                        new Max(new Fixed(0), new Sum(
                                new CardsInLibrary(CountScope.TARGET_PLAYER), new Fixed(-10))),
                        MillRecipient.TARGET_PLAYER))
        )));
    }
}
