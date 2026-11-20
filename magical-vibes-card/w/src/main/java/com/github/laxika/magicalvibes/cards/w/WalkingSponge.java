package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "47")
public class WalkingSponge extends Card {

    public WalkingSponge() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ChooseOneForTargetCreatureEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("It loses flying",
                                new RemoveKeywordEffect(Keyword.FLYING, Subjects.target())),
                        new ChooseOneEffect.ChooseOneOption("It loses first strike",
                                new RemoveKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target())),
                        new ChooseOneEffect.ChooseOneOption("It loses trample",
                                new RemoveKeywordEffect(Keyword.TRAMPLE, Subjects.target()))))),
                "{T}: Target creature loses your choice of flying, first strike, or trample until end of turn."
        ));
    }
}
