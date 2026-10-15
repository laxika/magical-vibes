package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.n.NassariDeanOfExpression;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandWithRefineCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "160")
public class UvildaDeanOfPerfection extends Card {

    public UvildaDeanOfPerfection() {
        NassariDeanOfExpression backFace = new NassariDeanOfExpression();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MayEffect(
                        new ExileCardFromHandWithRefineCountersEffect(
                                instantOrSorcery, 3, "an instant or sorcery card"),
                        "Exile an instant or sorcery card from your hand?")),
                "{T}: You may exile an instant or sorcery card from your hand and put three refine counters on it."));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Uvilda, Dean of Perfection", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Nassari, Dean of Expression", backFace.getEffects(EffectSlot.SPELL)))));
    }

    @Override
    public String getBackFaceClassName() {
        return "NassariDeanOfExpression";
    }
}
