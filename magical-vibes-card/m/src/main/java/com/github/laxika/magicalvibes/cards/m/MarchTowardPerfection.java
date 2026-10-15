package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCounterToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "9")
public class MarchTowardPerfection extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Archfiend of the Dross",
            "Bilious Skulldweller",
            "Diminished Returner",
            "Entomber Exarch",
            "Myr Convert",
            "Phyrexian Fleshgorger",
            "Phyrexian Gargantua",
            "Phyrexian Obliterator",
            "Phyrexian Rager",
            "Phyrexian Revoker",
            "Scrapwork Rager",
            "Soulless Jailer",
            "Toxic Abomination",
            "Vault Skirge",
            "Zenith Chronicler");

    public MarchTowardPerfection() {
        addEffect(EffectSlot.SPELL,
                RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.PHYREXIAN))),
                        List.of(
                                new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1),
                                new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                        CounterType.DEATHTOUCH, 1))));
        addEffect(EffectSlot.SPELL, new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
