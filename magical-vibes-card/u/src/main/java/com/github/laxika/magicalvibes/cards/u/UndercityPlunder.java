package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "YNEO", collectorNumber = "15")
public class UndercityPlunder extends Card {

    public UndercityPlunder() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addEffect(EffectSlot.SPELL,
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER))
                .addEffect(EffectSlot.SPELL, new MayEffect(
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                        "Discard an additional card?",
                        new ConjureRandomCardFromTargetPlayerLibraryEffect(),
                        MayChoicePlayer.TARGET_PLAYER));
    }
}
