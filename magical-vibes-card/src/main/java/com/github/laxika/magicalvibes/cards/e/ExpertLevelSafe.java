package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExpertLevelSafeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "133")
@CardRegistration(set = "PIP", collectorNumber = "661")
public class ExpertLevelSafe extends Card {

    public ExpertLevelSafe() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTopCardsToSourceEffect(2));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ExpertLevelSafeEffect()),
                "{1}, {T}: You and target opponent each secretly choose 1, 2, or 3. Then those choices are revealed. If they match, sacrifice this artifact and put all cards exiled with it into their owners' hands. Otherwise, exile the top card of your library face down.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));
    }
}
