package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YTDM", collectorNumber = "24")
public class OasisOfRenewal extends Card {

    public OasisOfRenewal() {
        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        CardNotPredicate nonland = new CardNotPredicate(land);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, OncePerTurnTriggerEffect.keyed(
                new SeekLibraryEffect(1, land), "land"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, OncePerTurnTriggerEffect.keyed(
                new SeekLibraryEffect(1, nonland), "nonland"));
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD, OncePerTurnTriggerEffect.keyed(
                new TriggeringCardConditionalEffect(land, new SeekLibraryEffect(1, land)), "land"));
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD, OncePerTurnTriggerEffect.keyed(
                new TriggeringCardConditionalEffect(nonland, new SeekLibraryEffect(1, nonland)), "nonland"));
    }
}
