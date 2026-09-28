package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.CopyEnchantedHandCardOnCastEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantCardInHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceEnchantedHandCardCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.HandCardPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "353")
public class DontWorryAboutIt extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("DontWorryAboutIt", new OracleData(
                "Don't Worry About It",
                CardType.ENCHANTMENT,
                Set.of(),
                "{1}{G}{U}",
                CardColor.GREEN,
                List.of(CardColor.GREEN, CardColor.BLUE),
                List.of(CardColor.GREEN, CardColor.BLUE),
                Set.of(),
                List.of(CardSubtype.AURA),
                "Enchant card in your hand. (This Aura remains on the battlefield. If you play enchanted "
                        + "card or it otherwise leaves your hand, put this Aura into its owner's graveyard.)\n"
                        + "Enchanted card costs {1} less to cast.\n"
                        + "When you cast enchanted card, copy it.",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public DontWorryAboutIt() {
        target(new HandCardPredicateTargetFilter(
                new CardTruePredicate(), "Target must be a card in your hand"));
        addEffect(EffectSlot.SPELL, new EnchantCardInHandEffect());
        addEffect(EffectSlot.STATIC, new ReduceEnchantedHandCardCastCostEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new CopyEnchantedHandCardOnCastEffect());
    }
}
