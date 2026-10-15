package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.ExileEnchantedCreatureAndSelfReturnAtNextTurnDeclareAttackersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "348")
@CardRegistration(set = "MB2", collectorNumber = "578")
public class MeanderedTowershell extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("MeanderedTowershell", new OracleData(
                "Meandered Towershell",
                CardType.ENCHANTMENT,
                Set.of(),
                "{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(CardSubtype.AURA),
                "Enchant creature\n"
                        + "Enchanted creature has islandwalk and \"Whenever this creature attacks, exile it and "
                        + "Meandered Towershell. Return it to the battlefield under your control tapped and attacking "
                        + "at the beginning of the declare attackers step on your next turn, then return Meandered "
                        + "Towershell to the battlefield under its owner's control attached to that creature.\"",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public MeanderedTowershell() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.ISLANDWALK, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.ON_ATTACK,
                        new ExileEnchantedCreatureAndSelfReturnAtNextTurnDeclareAttackersEffect());
    }
}
