package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EncoreEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.EumidianWastewakerEffect;
import java.util.List;

@CardRegistration(set = "EOC", collectorNumber = "8")
@CardRegistration(set = "EOC", collectorNumber = "28")
public class EumidianWastewaker extends Card {

    public EumidianWastewaker() {
        addEffect(EffectSlot.ON_ATTACK, new EumidianWastewakerEffect());
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{6}{B}{B}",
                List.of(new ExileSelfFromGraveyardCost(), new EncoreEffect()),
                "Encore {6}{B}{B}",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
