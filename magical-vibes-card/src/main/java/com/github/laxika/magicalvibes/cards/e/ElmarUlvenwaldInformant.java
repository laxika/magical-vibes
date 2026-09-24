package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "345")
public class ElmarUlvenwaldInformant extends Card {

    public ElmarUlvenwaldInformant() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new NthSpellCastTriggerEffect(
                2,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsCreaturePredicate()),
                        CreateTokenEffect.ofClueToken(1)
                )
        ));
    }
}
