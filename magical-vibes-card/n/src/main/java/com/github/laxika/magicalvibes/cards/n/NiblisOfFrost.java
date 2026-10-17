package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "72")
@CardRegistration(set = "PIO", collectorNumber = "66")
@CardRegistration(set = "C20", collectorNumber = "120")
public class NiblisOfFrost extends Card {

    public NiblisOfFrost() {
        // Prowess is auto-loaded from Scryfall keywords and handled by the engine's prowess trigger.
        // Whenever you cast an instant or sorcery spell, tap target creature an opponent controls.
        // That creature doesn't untap during its controller's next untap step.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                List.of(SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new SkipNextUntapEffect(TapUntapScope.TARGET))),
                null,
                TargetFilters.creatureAnOpponentControls()
        ));
    }
}
