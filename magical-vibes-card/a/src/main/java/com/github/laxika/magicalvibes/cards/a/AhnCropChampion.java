package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "194")
@CardRegistration(set = "AKR", collectorNumber = "226")
public class AhnCropChampion extends Card {

    public AhnCropChampion() {
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true),
                        new QueueReflexiveAbilityEffect(
                                new UntapPermanentsEffect(TapUntapScope.OTHER_CONTROLLED_CREATURES))
                ),
                "Exert Ahn-Crop Champion as it attacks? (Untap all other creatures you control.)"
        ));
    }
}
