package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YDFT", collectorNumber = "20")
public class RoutewayMoose extends Card {

    public RoutewayMoose() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new SourceIsSaddled(), new SeekLibraryEffect(
                        new CardTypePredicate(CardType.LAND), Integer.MAX_VALUE, true)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(2), new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)),
                "Saddle 2",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
