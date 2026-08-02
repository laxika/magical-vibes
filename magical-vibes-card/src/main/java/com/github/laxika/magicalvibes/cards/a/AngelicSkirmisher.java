package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "3")
public class AngelicSkirmisher extends Card {

    public AngelicSkirmisher() {
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("First strike", grantToCreaturesYouControl(Keyword.FIRST_STRIKE)),
                new ChooseOneEffect.ChooseOneOption("Vigilance", grantToCreaturesYouControl(Keyword.VIGILANCE)),
                new ChooseOneEffect.ChooseOneOption("Lifelink", grantToCreaturesYouControl(Keyword.LIFELINK))
        )));
    }

    private static List<CardEffect> grantToCreaturesYouControl(Keyword keyword) {
        return List.of(
                new GrantKeywordEffect(keyword, GrantScope.OWN_CREATURES),
                new GrantKeywordEffect(keyword, GrantScope.SELF)
        );
    }
}
