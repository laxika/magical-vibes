package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "85")
@CardRegistration(set = "V14", collectorNumber = "5")
public class DecreeOfAnnihilation extends Card {

    public DecreeOfAnnihilation() {
        addSpellEffect(ExileTargetPermanentEffect.all(new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate(),
                new PermanentIsLandPredicate()))));
        addSpellEffect(new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS));
        addSpellEffect(ExileTargetPlayerHandEffect.handsOf(Subjects.eachPlayer()));
        addTrigger(Triggers.youCycleThisCard(), DestroyTargetPermanentEffect.all(new PermanentIsLandPredicate()));
        addCycling("{5}{R}{R}");
    }
}
