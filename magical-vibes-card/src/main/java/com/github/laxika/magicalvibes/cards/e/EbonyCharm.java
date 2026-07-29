package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "120")
public class EbonyCharm extends Card {

    public EbonyCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent loses 1 life and you gain 1 life",
                        List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(1)),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent")),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile up to three target cards from a single graveyard",
                        new ExileCardsFromGraveyardEffect(3, 0)),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gains fear until end of turn",
                        new GrantKeywordEffect(Keyword.FEAR, GrantScope.TARGET),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature."))
        )));
    }
}
