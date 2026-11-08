package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySinglePermanentOrPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "194")
@CardRegistration(set = "MM2", collectorNumber = "225")
@CardRegistration(set = "BRR", collectorNumber = "42")
public class PrecursorGolem extends Card {

    public PrecursorGolem() {
        // When Precursor Golem enters the battlefield, create two 3/3 colorless Golem artifact creature tokens.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                2, "Golem", 3, 3, null, List.of(CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT)));

        // Whenever a player casts an instant or sorcery spell that targets only a single Golem,
        // that player copies that spell for each other Golem that spell could target.
        // Each copy targets a different one of those Golems.
        addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntryTargetsOnlySinglePermanentOrPlayerPredicate(),
                                new StackEntryTargetsPermanentPredicate(
                                        new PermanentHasSubtypePredicate(CardSubtype.GOLEM))))),
                new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM));
    }
}
