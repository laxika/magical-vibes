package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "266")
public class WaitingInTheWeeds extends Card {

    public WaitingInTheWeeds() {
        // Each player creates a 1/1 green Cat creature token for each untapped Forest they control.
        // EachPlayerCreatesTokenEffect re-evaluates the token count per creating player, so the
        // CONTROLLER-scoped count reads each player's own untapped Forests.
        addEffect(EffectSlot.SPELL, new EachPlayerCreatesTokenEffect(
                new CreateTokenEffect(
                        new PermanentCount(new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                                new PermanentNotPredicate(new PermanentIsTappedPredicate()))),
                                CountScope.CONTROLLER),
                        "Cat", 1, 1, CardColor.GREEN, List.of(CardSubtype.CAT), Set.of(), Set.of())));
    }
}
