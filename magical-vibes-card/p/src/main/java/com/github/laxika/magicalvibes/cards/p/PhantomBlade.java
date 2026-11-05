package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ACR", collectorNumber = "29")
public class PhantomBlade extends Card {

    public PhantomBlade() {
        var attachSourceEquipmentToTargetCreature = new AttachSourceEquipmentToTargetCreatureEffect();
        target(TargetFilters.creatureYouControl(), 0, 1)
                .bindEffect(attachSourceEquipmentToTargetCreature);
        var destroyTargetPermanent = new DestroyTargetPermanentEffect();
        target(TargetFilters.creature(), 0, 1)
                .bindEffect(destroyTargetPermanent);
        addTrigger(Triggers.enters(ObjectRelation.THIS), attachSourceEquipmentToTargetCreature, destroyTargetPermanent);

        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantKeywordEffect(Keyword.MENACE, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
