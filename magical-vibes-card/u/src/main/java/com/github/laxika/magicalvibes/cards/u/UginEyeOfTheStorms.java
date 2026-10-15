package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AllowCastCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "1")
public class UginEyeOfTheStorms extends Card {

    public UginEyeOfTheStorms() {
        PermanentPredicateTargetFilter coloredPermanent = new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsColorlessPredicate()),
                "Target must be a colored permanent");

        target(coloredPermanent, 0, 1)
                .addEffect(EffectSlot.ON_SELF_CAST, new ExileTargetPermanentEffect());

        target(coloredPermanent, 0, 1)
                .addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                        new CardIsColorlessPredicate(),
                        List.of(new ExileTargetPermanentEffect()),
                        null,
                        coloredPermanent));

        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new GainLifeEffect(3), new DrawCardEffect()),
                "+2: You gain 3 life and draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 3)),
                "0: Add {C}{C}{C}."
        ));

        CardAllOfPredicate colorlessNonland = new CardAllOfPredicate(List.of(
                new CardIsColorlessPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))
        ));
        addActivatedAbility(new ActivatedAbility(
                -11,
                List.of(
                        new SearchLibraryForCardsToExileWithSourceEffect(colorlessNonland),
                        new AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(colorlessNonland, true)
                ),
                "−11: Search your library for any number of colorless nonland cards, exile them, then shuffle. "
                        + "Until end of turn, you may cast those cards without paying their mana costs."
        ));
    }
}
