package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsOpponent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "9")
public class ScrivTheObligator extends Card {

    public ScrivTheObligator() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CreateTokenAttachedToTargetEffect(contractToken(), PlayerRelation.OPPONENT))
                .addEffect(EffectSlot.ON_ATTACK,
                        new CreateTokenAttachedToTargetEffect(contractToken(), PlayerRelation.OPPONENT));
    }

    private static CreateTokenEffect contractToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Contract",
                0,
                0,
                CardColor.WHITE,
                null,
                List.of(CardSubtype.AURA),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.ON_ATTACK, SequenceEffect.of(
                        new ConditionalEffect(new AttackedTargetIsOpponent(),
                                new BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect(2, 0)),
                        new ConditionalEffect(new NotCondition(new AttackedTargetIsOpponent()),
                                new EnchantedCreatureControllerLosesLifeEffect(2)))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
