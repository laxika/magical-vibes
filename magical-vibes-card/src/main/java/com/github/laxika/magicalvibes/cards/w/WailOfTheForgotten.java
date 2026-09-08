package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "244")
@CardRegistration(set = "LCI", collectorNumber = "343")
public class WailOfTheForgotten extends Card {

    public WailOfTheForgotten() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target nonland permanent to its owner's hand",
                        ReturnToHandEffect.target(), TargetFilters.nonlandPermanent()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent discards a card",
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Look at the top three cards of your library. Put one of them into your hand and the rest into your graveyard",
                        LookAtTopCardsEffect.chooseNToHandRestToGraveyard(3, 1))
        ), new GraveyardCardThreshold(8, new CardIsPermanentPredicate())));
    }
}
