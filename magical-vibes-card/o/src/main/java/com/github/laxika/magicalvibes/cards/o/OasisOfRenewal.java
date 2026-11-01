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
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YTDM", collectorNumber = "24")
public class OasisOfRenewal extends Card {

    public OasisOfRenewal() {
        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        CardNotPredicate nonland = new CardNotPredicate(land);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, OncePerTurnTriggerEffect.keyed(
                new SeekLibraryEffect(1, land), "land"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, OncePerTurnTriggerEffect.keyed(
                new SeekLibraryEffect(1, nonland), "nonland"));
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE, OncePerTurnTriggerEffect.keyed(
                new TriggeringCardConditionalEffect(land, new SeekLibraryEffect(1, land)), "land"));
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE, OncePerTurnTriggerEffect.keyed(
                new TriggeringCardConditionalEffect(nonland, new SeekLibraryEffect(1, nonland)), "nonland"));
    }
}
