package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.BrigidDounsMind;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "7")
@CardRegistration(set = "ECL", collectorNumber = "285")
public class BrigidClachansHeart extends Card {

    public BrigidClachansHeart() {
        setBackFaceCard(new BrigidDounsMind());

        CreateTokenEffect kithkinToken = new CreateTokenEffect(
                "Kithkin", 1, 1, CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.WHITE),
                List.of(CardSubtype.KITHKIN));

        // Whenever this creature enters or transforms into Brigid, Clachan's Heart, create a 1/1 green and white Kithkin creature token.
        addTrigger(Triggers.enters(ObjectRelation.THIS), kithkinToken);
        addTrigger(Triggers.transformsToFrontFace(ObjectRelation.THIS), kithkinToken);

        // At the beginning of your first main phase, you may pay {G}. If you do, transform Brigid.
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new MayPayManaEffect("{G}", new TransformSelfEffect(),
                        "Pay {G} to transform Brigid?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BrigidDounsMind";
    }
}
