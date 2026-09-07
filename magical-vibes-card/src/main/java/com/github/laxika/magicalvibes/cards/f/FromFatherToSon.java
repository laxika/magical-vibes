package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "FIN", collectorNumber = "20")
public class FromFatherToSon extends Card {

    public FromFatherToSon() {
        CardSubtypePredicate vehicle = new CardSubtypePredicate(CardSubtype.VEHICLE);
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastFromZone(Zone.GRAVEYARD)),
                new SearchLibraryEffect(vehicle, LibrarySearchDestination.HAND)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new SearchLibraryEffect(vehicle, LibrarySearchDestination.BATTLEFIELD)));
        addCastingOption(new FlashbackCast("{4}{W}{W}{W}"));
    }
}
