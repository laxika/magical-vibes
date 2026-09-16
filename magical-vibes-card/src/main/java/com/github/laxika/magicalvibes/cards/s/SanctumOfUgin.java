package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "PA1", collectorNumber = "11")
public class SanctumOfUgin extends Card {

    public SanctumOfUgin() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        CardAllOfPredicate colorlessSpellWithManaValueSeven = new CardAllOfPredicate(List.of(
                new CardIsColorlessPredicate(),
                new CardMinManaValuePredicate(7, true)));
        CardAllOfPredicate colorlessCreature = new CardAllOfPredicate(List.of(
                new CardIsColorlessPredicate(),
                new CardTypePredicate(CardType.CREATURE)));

        // Whenever you cast a colorless spell with mana value 7 or greater, you may sacrifice this
        // land. If you do, search your library for a colorless creature card, reveal it, put it into
        // your hand, then shuffle.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        colorlessSpellWithManaValueSeven,
                        List.of(new SacrificeSelfThenEffect(new SearchLibraryEffect(
                                colorlessCreature, LibrarySearchDestination.HAND)))),
                "Sacrifice Sanctum of Ugin?"));
    }
}
