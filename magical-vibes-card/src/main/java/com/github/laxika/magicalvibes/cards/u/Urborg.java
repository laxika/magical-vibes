package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "310")
public class Urborg extends Card {

    public Urborg() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ChooseOneForTargetCreatureEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("It loses first strike",
                                new RemoveKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)),
                        new ChooseOneEffect.ChooseOneOption("It loses swampwalk",
                                new RemoveKeywordEffect(Keyword.SWAMPWALK, GrantScope.TARGET))))),
                "{T}: Target creature loses first strike or swampwalk until end of turn."
        ));
    }
}
