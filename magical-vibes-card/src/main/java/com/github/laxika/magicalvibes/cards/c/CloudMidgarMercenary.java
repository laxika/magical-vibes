package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "10")
@CardRegistration(set = "FIN", collectorNumber = "375")
@CardRegistration(set = "FIN", collectorNumber = "427")
@CardRegistration(set = "FIN", collectorNumber = "520")
@CardRegistration(set = "FIN", collectorNumber = "564")
public class CloudMidgarMercenary extends Card {

    public CloudMidgarMercenary() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT)));

        addEffect(EffectSlot.STATIC, new AdditionalTriggeredAbilityEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsSourcePermanentPredicate(),
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                                new PermanentAttachedToSourcePermanentPredicate())))),
                new Equipped(), true, true));
    }
}
