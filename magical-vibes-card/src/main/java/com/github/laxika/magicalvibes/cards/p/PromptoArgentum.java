package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "148")
@CardRegistration(set = "FIN", collectorNumber = "387")
@CardRegistration(set = "FIN", collectorNumber = "463")
@CardRegistration(set = "FIN", collectorNumber = "532")
public class PromptoArgentum extends Card {

    public PromptoArgentum() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.withIntervening(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(CreateTokenEffect.ofTreasureToken(1)),
                new SpellManaSpentAtLeast(4)
        ));
    }
}
