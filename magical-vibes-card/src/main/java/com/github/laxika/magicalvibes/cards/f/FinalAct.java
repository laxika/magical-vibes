package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "213")
public class FinalAct extends Card {

    public FinalAct() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all creatures",
                        new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all planeswalkers",
                        new DestroyAllPermanentsEffect(new PermanentIsPlaneswalkerPredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all battles",
                        new DestroyAllPermanentsEffect(new PermanentIsBattlePredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile all graveyards",
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent loses all counters",
                        new EachOpponentLosesAllCountersEffect())
        )));
    }
}
