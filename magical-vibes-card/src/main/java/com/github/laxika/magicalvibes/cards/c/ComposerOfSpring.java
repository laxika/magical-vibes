package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "739")
@CardRegistration(set = "CMM", collectorNumber = "769")
public class ComposerOfSpring extends Card {

    private static final String LAND_PROMPT = "Put a land card from your hand onto the battlefield tapped?";
    private static final String CREATURE_OR_LAND_PROMPT =
            "Put a creature or land card from your hand onto the battlefield tapped?";

    public ComposerOfSpring() {
        ControlsPermanentCount sixEnchantments = new ControlsPermanentCount(
                6, new PermanentIsEnchantmentPredicate());
        PutCardToBattlefieldEffect land = new PutCardToBattlefieldEffect(
                new CardTypePredicate(CardType.LAND), "land", true);
        PutCardToBattlefieldEffect creatureOrLand = new PutCardToBattlefieldEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.LAND))), "creature or land", true);

        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, SequenceEffect.of(
                new ConditionalEffect(sixEnchantments,
                        new MayEffect(creatureOrLand, CREATURE_OR_LAND_PROMPT)),
                new ConditionalEffect(new NotCondition(sixEnchantments),
                        new MayEffect(land, LAND_PROMPT))));
    }
}
