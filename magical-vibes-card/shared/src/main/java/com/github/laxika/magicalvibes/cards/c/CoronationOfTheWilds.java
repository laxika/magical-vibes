package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentSupertypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

public class CoronationOfTheWilds extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("CoronationOfTheWilds", new OracleData(
                "Coronation of the Wilds",
                CardType.SORCERY,
                Set.of(),
                "{2}{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(),
                "Target creature you control becomes a legendary Noble in addition to its other types and gains "
                        + "\"{T}: Add one mana of any color.\"\nDraw a card.",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public CoronationOfTheWilds() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new SetTargetPermanentSupertypeEffect(CardSupertype.LEGENDARY, true))
                .addEffect(EffectSlot.SPELL, new GrantSubtypeEffect(CardSubtype.NOBLE, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL, new GrantActivatedAbilityEffect(
                        ManaAbilities.tapForAnyColor(), GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
