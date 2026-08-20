package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "215")
public class RediscoverTheWay extends Card {

    private static final CardNotPredicate NONCREATURE_SPELL =
            new CardNotPredicate(new CardTypePredicate(CardType.CREATURE));
    private static final PermanentPredicate CREATURE_YOU_CONTROL = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentControlledBySourceControllerPredicate()));

    public RediscoverTheWay() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new RegisterDelayedControllerSpellCastTriggerEffect(
                        NONCREATURE_SPELL,
                        List.of(new GrantKeywordEffect(
                                Keyword.DOUBLE_STRIKE, GrantScope.TARGET, CREATURE_YOU_CONTROL)),
                        false,
                        new PermanentPredicateTargetFilter(
                                CREATURE_YOU_CONTROL, "Target must be a creature you control")));
    }
}
