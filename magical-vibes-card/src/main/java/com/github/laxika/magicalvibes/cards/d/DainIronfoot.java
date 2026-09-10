package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachCreatedEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "91")
public class DainIronfoot extends Card {

    public DainIronfoot() {
        Map<EffectSlot, CardEffect> axeEffects = Map.of(
                EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));
        CreateTokenEffect axe = new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Axe", 0, 0,
                null, null, List.of(CardSubtype.EQUIPMENT), Set.of(), Set.of(),
                false, false, axeEffects, List.of(new EquipActivatedAbility("{2}")),
                false, false, false, 0, Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenThenEffect(axe, new AttachCreatedEquipmentToTargetCreatureEffect()));

        var equippedAttackingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate(),
                new PermanentIsEquippedPredicate()));
        addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(
                Keyword.DOUBLE_STRIKE, GrantScope.ALL_CREATURES_INCLUDING_SELF,
                equippedAttackingCreature));
    }
}
