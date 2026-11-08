package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPermanentOrPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetCreatureForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasOneTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OHOP", collectorNumber = "12")
@CardRegistration(set = "OPCA", collectorNumber = "29")
@CardRegistration(set = "MOC", collectorNumber = "143")
public class GlimmervoidBasin extends Card {

    public GlimmervoidBasin() {
        addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryHasOneTargetPredicate()),
                new CopySpellForEachOtherPermanentOrPlayerEffect());
        target(TargetFilters.creature()).addTrigger(Triggers.chaosEnsues(),
                new CreateTokenCopyOfTargetCreatureForEachOtherPlayerEffect());
    }
}
