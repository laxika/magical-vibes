package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "FIN", collectorNumber = "247")
@CardRegistration(set = "FIN", collectorNumber = "513")
public class UltimeciaTimeSorceress extends Card {

    public UltimeciaTimeSorceress() {
        setBackFaceCard(new UltimeciaOmnipotent());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(2));
        addEffect(EffectSlot.ON_ATTACK, new SurveilEffect(2));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                ConditionalEffect.unless(
                        new GraveyardCardThreshold(8, null),
                        new MayPayManaEffect(
                                "{4}{U}{U}{B}{B}",
                                SequenceEffect.of(
                                        new ExileGraveyardCardsEffect(8, GraveyardExileScope.OWN),
                                        new TransformSelfEffect()),
                                "Pay {4}{U}{U}{B}{B} and exile eight cards from your graveyard?")));
    }

    @Override
    public String getBackFaceClassName() {
        return "UltimeciaOmnipotent";
    }
}
