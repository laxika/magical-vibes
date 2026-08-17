package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "225")
public class FindFinality extends Card {

    public FindFinality() {
        CardEffect find = new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardTypePredicate(CardType.CREATURE), 2);
        List<CardEffect> finality = List.of(
                new MayEffect(
                        new PutCounterOnTargetPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 2, new PermanentIsCreaturePredicate()),
                        "Put two +1/+1 counters on a creature you control?"),
                new BoostAllCreaturesEffect(-4, -4));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Find - Return up to two target creature cards from your graveyard to your hand",
                        find).withManaCost("{B/G}{B/G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Finality - Put two +1/+1 counters on a creature you control, then all creatures get -4/-4 until end of turn",
                        finality).withManaCost("{4}{B}{G}")
        )));
    }
}
