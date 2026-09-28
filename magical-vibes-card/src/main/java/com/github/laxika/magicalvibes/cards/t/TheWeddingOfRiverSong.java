package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNonlandCardFromHandWithManaValueTimeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "WHO", collectorNumber = "31")
@CardRegistration(set = "WHO", collectorNumber = "349")
public class TheWeddingOfRiverSong extends Card {

    public TheWeddingOfRiverSong() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new MayEffect(
                new ExileNonlandCardFromHandWithManaValueTimeCountersEffect(),
                "Exile a nonland card from your hand?"));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent")).addEffect(EffectSlot.SPELL, new MayEffect(
                ExileNonlandCardFromHandWithManaValueTimeCountersEffect.forTargetPlayer(),
                "Exile a nonland card from your hand?",
                null,
                MayChoicePlayer.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new TimeTravelEffect(1));
    }
}
