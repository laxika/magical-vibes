package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@CardRegistration(set = "YMID", collectorNumber = "6")
public class ExpeditionSupplier extends Card {

    public ExpeditionSupplier() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.HUMAN),
                                new CardSubtypePredicate(CardSubtype.WARRIOR))),
                        new OncePerTurnTriggerEffect(utilityKnifeToken())));
    }

    private static CreateTokenEffect utilityKnifeToken() {
        Map<EffectSlot, CardEffect> effects = new EnumMap<>(EffectSlot.class);
        effects.put(EffectSlot.ON_ENTER_BATTLEFIELD, new AttachSourceEquipmentToTargetCreatureEffect());
        effects.put(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));

        return CreateTokenEffect.ofArtifactToken(
                        1, "Utility Knife", List.of(CardSubtype.EQUIPMENT),
                        List.of(new EquipActivatedAbility("{3}")))
                .withTokenEffects(effects)
                .withTokenTargetFilter(TargetFilters.creatureYouControl());
    }
}
