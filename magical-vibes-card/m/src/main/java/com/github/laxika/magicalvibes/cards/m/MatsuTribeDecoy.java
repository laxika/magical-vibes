package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "227")
public class MatsuTribeDecoy extends Card {

    public MatsuTribeDecoy() {
        // {2}{G}: Target creature blocks this creature this turn if able. The null source id is
        // snapshotted from the activating permanent at resolution.
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}", List.of(new MustBlockSourceEffect(null)),
                "{2}{G}: Target creature blocks Matsu-Tribe Decoy this turn if able."));

        // Whenever this creature deals combat damage to a creature, tap that creature and it doesn't
        // untap during its controller's next untap step. The damaged creature is baked as targetId by
        // Triggers.dealsCombatDamageToCreature() (non-targeting), so both halves act on it.
        addTrigger(Triggers.dealsCombatDamageToCreature(), SequenceEffect.of(
                new TapPermanentsEffect(TapUntapScope.TARGET),
                new SkipNextUntapEffect(TapUntapScope.TARGET)));
    }
}
