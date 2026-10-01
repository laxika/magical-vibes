package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromEverythingUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "11")
@CardRegistration(set = "BLC", collectorNumber = "47")
public class PerchProtection extends Card {

    public PerchProtection() {
        addEffect(EffectSlot.STATIC, new GiftEffect());
        targetWhenGiftPromised(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ), 0, 1, 1).addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new GiftPromised(), new ExtraTurnEffect(1)));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(4, "Bird", 2, 2,
                CardColor.BLUE, List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                new PhaseOutPermanentsEffect(new PermanentTruePredicate(), true)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                new LifeTotalCantChangeUntilNextTurnEffect()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                new GrantProtectionFromEverythingUntilNextTurnEffect()));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
