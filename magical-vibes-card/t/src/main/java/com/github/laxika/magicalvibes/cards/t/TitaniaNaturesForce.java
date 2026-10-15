package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "202")
@CardRegistration(set = "BRC", collectorNumber = "25")
@CardRegistration(set = "BRC", collectorNumber = "45")
public class TitaniaNaturesForce extends Card {

    public TitaniaNaturesForce() {
        addEffect(EffectSlot.STATIC,
                new PlayLandsFromGraveyardEffect(new CardSubtypePredicate(CardSubtype.FOREST)));

        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.FOREST),
                        new CreateTokenEffect("Elemental", 5, 3,
                                CardColor.GREEN, List.of(CardSubtype.ELEMENTAL),
                                Set.of(), Set.of())));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL),
                        new MayEffect(new MillEffect(3, MillRecipient.CONTROLLER),
                                "Mill three cards?")));
    }
}
