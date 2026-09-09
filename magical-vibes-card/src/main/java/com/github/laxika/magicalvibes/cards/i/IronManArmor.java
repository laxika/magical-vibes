package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "248")
public class IronManArmor extends Card {

    public IronManArmor() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AttachSourceEquipmentToTargetCreatureEffect());

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FLYING, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 1, GrantScope.EQUIPPED_CREATURE));

        var artifactsYouControl = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new ConditionalEffect(
                        new NotCondition(new SourceIsCreature()),
                        AnimatePermanentsEffect.withDynamicPowerToughness(
                                artifactsYouControl, artifactsYouControl,
                                List.of(CardSubtype.CONSTRUCT, CardSubtype.HERO),
                                Set.of(Keyword.FLYING), Set.of()))),
                "{2}: If this Equipment isn't a creature, it becomes a 0/0 Construct Hero artifact creature "
                        + "with flying and \"This creature gets +1/+1 for each artifact you control\" until end of turn."
        ));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
