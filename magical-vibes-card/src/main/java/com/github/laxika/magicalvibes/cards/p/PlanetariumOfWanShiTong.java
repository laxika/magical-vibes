package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "259")
public class PlanetariumOfWanShiTong extends Card {

    private static final Set<CardType> CASTABLE_CARD_TYPES = Set.of(
            CardType.CREATURE,
            CardType.ENCHANTMENT,
            CardType.SORCERY,
            CardType.INSTANT,
            CardType.ARTIFACT,
            CardType.PLANESWALKER,
            CardType.BATTLE);

    public PlanetariumOfWanShiTong() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ScryEffect(2)),
                "{1}, {T}: Scry 2."));

        addEffect(EffectSlot.ON_CONTROLLER_SCRIES, OncePerTurnTriggerEffect.markOnAcceptance(
                new CastTopOfLibraryWithoutPayingManaCostEffect(CASTABLE_CARD_TYPES)));
        addEffect(EffectSlot.ON_CONTROLLER_SURVEILS, OncePerTurnTriggerEffect.markOnAcceptance(
                new CastTopOfLibraryWithoutPayingManaCostEffect(CASTABLE_CARD_TYPES)));
    }
}
