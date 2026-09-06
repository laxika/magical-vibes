package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "218")
public class EarthRumbleWrestlers extends Card {

    public EarthRumbleWrestlers() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AnyOf(List.of(
                        new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentIsCreaturePredicate()))),
                        new PermanentEnteredThisTurn(new CardTypePredicate(CardType.LAND), 1))),
                new StaticBoostEffect(1, 0, Set.of(Keyword.TRAMPLE), GrantScope.SELF)));
    }
}
