package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RememberTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "67")
@CardRegistration(set = "A25", collectorNumber = "95")
@CardRegistration(set = "VMA", collectorNumber = "125")
public class LaquatussChampion extends Card {

    public LaquatussChampion() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RememberTargetPlayerEffect(),
                new LoseLifeEffect(6, LoseLifeRecipient.TARGET_PLAYER));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new GainLifeEffect(new Fixed(6), GainLifeRecipient.SOURCE_REMEMBERED_PLAYER));
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()),
                "{B}: Regenerate Laquatus's Champion."));
    }
}
