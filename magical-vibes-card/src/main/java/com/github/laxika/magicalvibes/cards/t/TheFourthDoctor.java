package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateFoodWhenPlayingCardFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandOrCastSpellFromTopOfLibraryOncePerTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

@CardRegistration(set = "WHO", collectorNumber = "2")
@CardRegistration(set = "WHO", collectorNumber = "193")
public class TheFourthDoctor extends Card {

    public TheFourthDoctor() {
        CardIsHistoricPredicate historic = new CardIsHistoricPredicate();

        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC,
                new PlayLandOrCastSpellFromTopOfLibraryOncePerTurnEffect(historic));

        CreateFoodWhenPlayingCardFromTopOfLibraryEffect createFood =
                new CreateFoodWhenPlayingCardFromTopOfLibraryEffect(historic);
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new OncePerTurnTriggerEffect(createFood));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new OncePerTurnTriggerEffect(createFood));
    }
}
