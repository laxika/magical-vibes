package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "161")
@CardRegistration(set = "AKR", collectorNumber = "272")
@CardRegistration(set = "DRC", collectorNumber = "131")
public class GodPharaohsGift extends Card {

    public GodPharaohsGift() {
        // At the beginning of combat on your turn, you may exile a creature card from your graveyard.
        // If you do, create a token that's a copy of that card, except it's a 4/4 black Zombie.
        // It gains haste until end of turn.
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                SequenceEffect.of(
                        ExileUpToOneCardFromGraveyardEffect.matching(new CardTypePredicate(CardType.CREATURE))
                                .from(Subjects.you()),
                        CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                                        new CreateTokenCopyOfTargetPermanentEffect(List.of(), Set.of(), 4, 4, Map.of(), true,
                                                false, false, false, false, false, CardColor.BLACK, Set.of(), false,
                                                List.of(), List.of(), false, false, new Fixed(1), false, Set.of(), false,
                                                List.of(), null, false, true))
                                .withCreatureTypesInstead(List.of(CardSubtype.ZOMBIE))));
    }
}
