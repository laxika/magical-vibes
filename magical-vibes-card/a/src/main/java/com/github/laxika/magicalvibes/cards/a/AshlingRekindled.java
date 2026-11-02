package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "124")
@CardRegistration(set = "ECL", collectorNumber = "290")
public class AshlingRekindled extends Card {

    public AshlingRekindled() {
        setBackFaceCard(new AshlingRimebound());

        MayEffect discardAndDraw = new MayEffect(
                new DiscardAndDrawCardEffect(), "Discard a card to draw a card?");
        addTrigger(Triggers.enters(ObjectRelation.THIS), discardAndDraw);
        addTrigger(Triggers.transformsToFrontFace(ObjectRelation.THIS), discardAndDraw);

        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new MayPayManaEffect("{U}", new TransformSelfEffect(),
                        "Pay {U} to transform Ashling?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "AshlingRimebound";
    }
}
