package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "ALA", collectorNumber = "164")
@CardRegistration(set = "DDH", collectorNumber = "69")
@CardRegistration(set = "V13", collectorNumber = "17")
@CardRegistration(set = "MM3", collectorNumber = "158")
@CardRegistration(set = "OTP", collectorNumber = "40")
@CardRegistration(set = "C13", collectorNumber = "182")
@CardRegistration(set = "E01", collectorNumber = "82")
public class CruelUltimatum extends Card {

    public CruelUltimatum() {
        // Target opponent sacrifices a creature of their choice, discards three cards, then loses 5 life.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                        1, new PermanentIsCreaturePredicate(), SacrificeRecipient.TARGET_PLAYER))
                .addEffect(EffectSlot.SPELL, new DiscardEffect(3, DiscardRecipient.TARGET_PLAYER))
                .addEffect(EffectSlot.SPELL, new LoseLifeEffect(5, LoseLifeRecipient.TARGET_PLAYER));

        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .mandatory(true)
                .build());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(5));
    }
}
