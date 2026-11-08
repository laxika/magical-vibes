package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "155")
public class NekoTe extends Card {

    public NekoTe() {
        addTrigger(Triggers.equippedCreatureDealsDamageToCreature(),
                new TapPermanentsEffect(TapUntapScope.TRIGGERING),
                DoesntUntapEffect.triggeringWhileSourceOnBattlefield());
        addTrigger(Triggers.dealsDamageToPlayer(),
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
