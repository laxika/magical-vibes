package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "196")
@CardRegistration(set = "MSC", collectorNumber = "429")
@CardRegistration(set = "SLD", collectorNumber = "799")
@CardRegistration(set = "BLC", collectorNumber = "268")
@CardRegistration(set = "M3C", collectorNumber = "88")
@CardRegistration(set = "M3C", collectorNumber = "287")
@CardRegistration(set = "FIC", collectorNumber = "341")
@CardRegistration(set = "MOC", collectorNumber = "353")
@CardRegistration(set = "C21", collectorNumber = "240")
@CardRegistration(set = "C18", collectorNumber = "54")
@CardRegistration(set = "SCD", collectorNumber = "262")
public class CovetedJewel extends Card {

    public CovetedJewel() {
        // When this artifact enters, draw three cards.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(3));

        // {T}: Add three mana of any one color.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(3)),
                "{T}: Add three mana of any one color."
        ));

        // Whenever one or more creatures an opponent controls attack you and aren't blocked, that
        // player draws three cards and gains control of this artifact. Untap it.
        addTrigger(Triggers.creaturesAttackYouUnblocked(),
                SequenceEffect.of(
                        new DrawCardForTargetPlayerEffect(3),
                        TargetPlayerGainsControlOfSourceCreatureEffect.triggeringPlayer(),
                        new UntapPermanentsEffect(TapUntapScope.SOURCE_PERMANENT)
                ));
    }
}
