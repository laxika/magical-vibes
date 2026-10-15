package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "236")
public class ChromeCompanion extends Card {

    public ChromeCompanion() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceCardPredicate(),
                        new GainLifeEffect(1)));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect(
                        PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect.Destination.BOTTOM)),
                "{2}, {T}: Put target card from a graveyard on the bottom of its owner's library."
        ));
    }
}
