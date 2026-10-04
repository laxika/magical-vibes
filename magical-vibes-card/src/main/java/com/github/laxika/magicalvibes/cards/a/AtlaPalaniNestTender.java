package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1397")
@CardRegistration(set = "2X2", collectorNumber = "177")
@CardRegistration(set = "DMC", collectorNumber = "142")
@CardRegistration(set = "C19", collectorNumber = "38")
public class AtlaPalaniNestTender extends Card {

    public AtlaPalaniNestTender() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new CreateTokenEffect(
                        "Egg", 0, 1, CardColor.GREEN, List.of(CardSubtype.EGG),
                        Set.of(Keyword.DEFENDER), Set.of())),
                "{2}, {T}: Create a 0/1 green Egg creature token with defender."
        ));

        var eggDeath = new TriggeringPermanentConditionalEffect(
                new PermanentHasSubtypePredicate(CardSubtype.EGG),
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardTypePredicate(CardType.CREATURE), LibrarySearchDestination.BATTLEFIELD));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, eggDeath);
        addEffect(EffectSlot.ON_DEATH, eggDeath);
    }
}
