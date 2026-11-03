package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "97")
@CardRegistration(set = "WOE", collectorNumber = "339")
@CardRegistration(set = "SLZ", collectorNumber = "46")
@CardRegistration(set = "SLZ", collectorNumber = "167")
@CardRegistration(set = "SLZ", collectorNumber = "288")
public class LordSkitterSewerKing extends Card {

    public LordSkitterSewerKing() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.RAT))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                ExileGraveyardCardsEffect.upToOneTargetFromOpponentGraveyard());
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new CreateTokenEffect(1, "Rat", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.RAT), Set.of(), Set.of(),
                        List.of(StaticAbility.of(new CantBlockEffect()))));
    }
}
