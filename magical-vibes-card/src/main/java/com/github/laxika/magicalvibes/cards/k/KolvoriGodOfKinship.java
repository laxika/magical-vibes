package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TheRinghartCrest;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "181")
public class KolvoriGodOfKinship extends Card {

    public KolvoriGodOfKinship() {
        setBackFaceCard(new TheRinghartCrest());
        setModalDoubleFaced(true);

        ControlsPermanentCount legendaryCreatureThreshold = new ControlsPermanentCount(3,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)
                )));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(legendaryCreatureThreshold,
                new StaticBoostEffect(4, 2, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(legendaryCreatureThreshold,
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(true, "{1}{G}", List.of(
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(6,
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSupertypePredicate(CardSupertype.LEGENDARY)
                        )))
        ), "{1}{G}, {T}: Look at the top six cards of your library. You may reveal a legendary creature card from among them and put it into your hand. Put the rest on the bottom of your library in a random order."));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Kolvori, God of Kinship", List.of())
                        .withManaCost("{2}{G}{G}"),
                new ChooseOneEffect.ChooseOneOption("The Ringhart Crest", List.of())
                        .withManaCost("{1}{G}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheRinghartCrest";
    }
}
