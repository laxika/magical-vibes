package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAsEntersOrGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "138")
public class HeartOfYavimaya extends Card {

    public HeartOfYavimaya() {
        // If Heart of Yavimaya would enter, sacrifice a Forest instead. If you do, put this land
        // onto the battlefield. If you don't, put it into its owner's graveyard.
        addEffect(EffectSlot.STATIC, new SacrificePermanentAsEntersOrGraveyardEffect(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                "a Forest"));

        // {T}: Add {G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {G}."
        ));

        // {T}: Target creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(1, 1)),
                "{T}: Target creature gets +1/+1 until end of turn."
        ));
    }
}
