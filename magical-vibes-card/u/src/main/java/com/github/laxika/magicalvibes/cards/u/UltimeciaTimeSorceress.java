package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.AllOfCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "247")
@CardRegistration(set = "FIN", collectorNumber = "513")
public class UltimeciaTimeSorceress extends Card {

    public UltimeciaTimeSorceress() {
        setBackFaceCard(new UltimeciaOmnipotent());

        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(2));
        addTrigger(Triggers.attacks(), new SurveilEffect(2));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                UnlessPaysEffect.youMayPay(AllOfCost.of(new PayManaCost("{4}{U}{U}{B}{B}"),
                                new ExileNCardsFromGraveyardCost(8, null)), new TransformSelfEffect())
                        .withPrompt("Pay {4}{U}{U}{B}{B} and exile eight cards from your graveyard?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "UltimeciaOmnipotent";
    }
}
