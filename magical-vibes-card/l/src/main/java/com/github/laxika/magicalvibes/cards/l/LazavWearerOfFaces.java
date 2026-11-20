package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "216")
@CardRegistration(set = "MKM", collectorNumber = "318")
@CardRegistration(set = "MKM", collectorNumber = "366")
public class LazavWearerOfFaces extends Card {

    public LazavWearerOfFaces() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, true),
                CreateTokenEffect.ofClueToken(1)));
        addTrigger(Triggers.sacrificed(new PermanentHasSubtypePredicate(CardSubtype.CLUE), ObjectRelation.YOU_CONTROL),
                new MayEffect(
                        new BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect(),
                        "Have Lazav become a copy of a creature card exiled with it until end of turn?"));
    }
}
