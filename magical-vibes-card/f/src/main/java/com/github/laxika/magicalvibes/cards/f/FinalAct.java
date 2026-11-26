package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "213")
@CardRegistration(set = "MAR", collectorNumber = "66")
@CardRegistration(set = "M3C", collectorNumber = "52")
@CardRegistration(set = "M3C", collectorNumber = "104")
public class FinalAct extends Card {

    public FinalAct() {
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all creatures",
                        DestroyTargetPermanentEffect.all(new PermanentIsCreaturePredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all planeswalkers",
                        DestroyTargetPermanentEffect.all(new PermanentIsPlaneswalkerPredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all battles",
                        DestroyTargetPermanentEffect.all(new PermanentIsBattlePredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile all graveyards",
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent loses all counters",
                        RemoveAllCountersFromTargetPermanentEffect.fromPlayers(Subjects.eachOpponent()))
        )));
    }
}
