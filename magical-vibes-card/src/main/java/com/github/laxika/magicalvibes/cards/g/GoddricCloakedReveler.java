package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "132")
public class GoddricCloakedReveler extends Card {

    public GoddricCloakedReveler() {
        PermanentEnteredThisTurn celebration = new PermanentEnteredThisTurn(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 2);

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                celebration, new GrantSubtypeEffect(CardSubtype.DRAGON, GrantScope.SELF, true)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                celebration, new SetBasePowerToughnessEffect(4, 4, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                celebration, new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                celebration,
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{R}",
                                List.of(new BoostAllOwnCreaturesEffect(
                                        1, 0, new PermanentHasSubtypePredicate(CardSubtype.DRAGON))),
                                "{R}: Dragons you control get +1/+0 until end of turn."),
                        GrantScope.SELF)));
    }
}
