package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "WHO", collectorNumber = "101")
public class TheFiveDoctors extends Card {

    public TheFiveDoctors() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{5}"));
        CardSubtypePredicate doctor = new CardSubtypePredicate(CardSubtype.DOCTOR);
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Kicked(),
                new SearchLibraryAndOrGraveyardForCardsEffect(doctor, 5),
                new SearchLibraryAndOrGraveyardForCardsEffect(
                        doctor, 5, LibrarySearchDestination.BATTLEFIELD)));
    }
}
