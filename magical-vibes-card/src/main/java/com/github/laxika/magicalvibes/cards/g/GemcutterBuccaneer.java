package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

@CardRegistration(set = "LCC", collectorNumber = "55")
@CardRegistration(set = "LCC", collectorNumber = "87")
public class GemcutterBuccaneer extends Card {

    public GemcutterBuccaneer() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.PIRATE),
                        CreateTokenEffect.ofTappedTreasureToken(1)));

        PermanentPredicate treasure = new PermanentHasSubtypePredicate(CardSubtype.TREASURE);

        addEffect(EffectSlot.STATIC,
                new GrantSubtypeEffect(CardSubtype.EQUIPMENT, GrantScope.OWN_PERMANENTS, false, treasure));
        addEffect(EffectSlot.STATIC,
                new GrantEffectEffect(
                        new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE),
                        GrantScope.OWN_PERMANENTS,
                        treasure));
        addEffect(EffectSlot.STATIC,
                new GrantActivatedAbilityEffect(
                        new EquipActivatedAbility(
                                "{1}",
                                new PermanentHasSubtypePredicate(CardSubtype.PIRATE),
                                "Target must be a Pirate you control"),
                        GrantScope.OWN_PERMANENTS,
                        treasure));
        addEffect(EffectSlot.STATIC,
                new GrantActivatedAbilityEffect(
                        new EquipActivatedAbility("{3}"),
                        GrantScope.OWN_PERMANENTS,
                        treasure));
    }
}
