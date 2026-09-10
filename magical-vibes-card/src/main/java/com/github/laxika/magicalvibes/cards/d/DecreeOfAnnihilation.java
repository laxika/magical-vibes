package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllHandsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "85")
public class DecreeOfAnnihilation extends Card {

    public DecreeOfAnnihilation() {
        addEffect(EffectSlot.SPELL, new ExileAllPermanentsEffect(new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate(),
                new PermanentIsLandPredicate()))));
        addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS));
        addEffect(EffectSlot.SPELL, new ExileAllHandsEffect());
        addEffect(EffectSlot.ON_SELF_CYCLED, new DestroyAllPermanentsEffect(new PermanentIsLandPredicate()));
        addCycling("{5}{R}{R}");
    }
}
