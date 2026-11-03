package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OHOP", collectorNumber = "19")
@CardRegistration(set = "OPCA", collectorNumber = "40")
public class IzzetSteamMaze extends Card {

    public IzzetSteamMaze() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addTrigger(Triggers.playerCastsSpell().withFilter(instantOrSorcery), new CopyTriggeringSpellEffect());
        addTrigger(Triggers.chaosEnsues(),
                new ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(instantOrSorcery, 3));
    }
}
