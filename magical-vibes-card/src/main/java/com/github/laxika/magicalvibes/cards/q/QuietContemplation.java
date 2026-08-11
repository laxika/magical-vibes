package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "50")
public class QuietContemplation extends Card {

    public QuietContemplation() {
        // Whenever you cast a noncreature spell, you may pay {1}. If you do, tap target creature
        // an opponent controls and it doesn't untap during its controller's next untap step.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new SkipNextUntapEffect(TapUntapScope.TARGET))),
                "{1}",
                TargetFilters.creatureAnOpponentControls()
        ));
    }
}
