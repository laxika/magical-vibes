package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "149")
@CardRegistration(set = "MMA", collectorNumber = "217")
@CardRegistration(set = "MPS", collectorNumber = "30")
@CardRegistration(set = "2XM", collectorNumber = "298")
@CardRegistration(set = "AA1", collectorNumber = "14")
@CardRegistration(set = "ACR", collectorNumber = "100")
@CardRegistration(set = "ACR", collectorNumber = "125")
public class SwordOfLightAndShadow extends Card {

    public SwordOfLightAndShadow() {
        addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new ProtectionFromColorsEffect(
                Set.of(CardColor.WHITE, CardColor.BLACK), GrantScope.EQUIPPED_CREATURE));
        // One ability: if the chosen graveyard target is illegal on resolution it does nothing at all,
        // including the life gain (CR 608.2b).
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER,
                SequenceEffect.of(
                        new GainLifeEffect(3),
                        new MayEffect(ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .upTo(true)
                                .build(), "Return the targeted creature card to your hand?")));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
