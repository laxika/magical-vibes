package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellXValue;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "147")
public class PhotonBlastBarrage extends Card {

    public PhotonBlastBarrage() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(1));
        addTrigger(Triggers.youCastThisSpell(),
                new CopyTriggeringSpellEffect().withCopies(new TriggeringSpellXValue()));
    }
}
