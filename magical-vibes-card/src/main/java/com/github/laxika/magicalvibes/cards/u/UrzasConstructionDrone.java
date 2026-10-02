package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "29")
public class UrzasConstructionDrone extends Card {

    public UrzasConstructionDrone() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new ConjureCardNamedIntoLibraryEffect("Urza's Mine", 1),
                new ConjureCardNamedIntoLibraryEffect("Urza's Power Plant", 1),
                new ConjureCardNamedIntoLibraryEffect("Urza's Tower", 1)));

        CardAllOfPredicate urzasLand = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND),
                new CardSubtypePredicate(CardSubtype.URZAS)));
        SeekLibraryEffect seekUrzasLand = new SeekLibraryEffect(1, urzasLand);
        addEffect(EffectSlot.ON_ATTACK, seekUrzasLand);
        addEffect(EffectSlot.ON_DEATH, seekUrzasLand);
    }
}
