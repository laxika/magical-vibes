package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "3")
public class AngelicSkirmisher extends Card {

    public AngelicSkirmisher() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY),
                new ChooseOneAtResolutionEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("First strike", grantToCreaturesYouControl(Keyword.FIRST_STRIKE)),
                new ChooseOneEffect.ChooseOneOption("Vigilance", grantToCreaturesYouControl(Keyword.VIGILANCE)),
                new ChooseOneEffect.ChooseOneOption("Lifelink", grantToCreaturesYouControl(Keyword.LIFELINK))
        ))));
    }

    private static List<CardEffect> grantToCreaturesYouControl(Keyword keyword) {
        return List.of(
                new GrantKeywordEffect(keyword, GrantScope.ALL_OWN_CREATURES)
        );
    }
}
