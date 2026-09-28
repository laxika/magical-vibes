package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "30")
public class SwornToTheLegion extends Card {

    public SwornToTheLegion() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyGrantKeywordToMatchingCardsEffect(
                        new CardNotPredicate(new CardTruePredicate()),
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        Set.of(Keyword.DOUBLE_TEAM)));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new PerpetuallyGrantKeywordsToTriggeringCardEffect(Set.of(Keyword.DOUBLE_TEAM)))));
    }
}
