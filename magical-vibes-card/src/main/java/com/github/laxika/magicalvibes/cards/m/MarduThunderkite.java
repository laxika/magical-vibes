package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YTDM", collectorNumber = "23")
public class MarduThunderkite extends Card {

    public MarduThunderkite() {
        var noCardsInHand = new CardNotPredicate(new CardTruePredicate());
        var creaturesYouControl = new PermanentIsCreaturePredicate();

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Menace",
                                grantKeyword(Keyword.MENACE, noCardsInHand, creaturesYouControl)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Lifelink",
                                grantKeyword(Keyword.LIFELINK, noCardsInHand, creaturesYouControl)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Haste",
                                grantKeyword(Keyword.HASTE, noCardsInHand, creaturesYouControl))
                ))));
    }

    private static SequenceEffect grantKeyword(Keyword keyword,
                                                 CardNotPredicate noCardsInHand,
                                                 PermanentIsCreaturePredicate creaturesYouControl) {
        return SequenceEffect.of(
                new PerpetuallyGrantKeywordToSourceEffect(keyword),
                new PerpetuallyGrantKeywordToMatchingCardsEffect(
                        noCardsInHand, creaturesYouControl, Set.of(keyword)));
    }
}
