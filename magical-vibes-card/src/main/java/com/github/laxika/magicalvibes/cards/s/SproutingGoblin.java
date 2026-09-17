package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "145")
public class SproutingGoblin extends Card {

    public SproutingGoblin() {
        var landWithBasicLandType = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND),
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.PLAINS),
                        new CardSubtypePredicate(CardSubtype.ISLAND),
                        new CardSubtypePredicate(CardSubtype.SWAMP),
                        new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                        new CardSubtypePredicate(CardSubtype.FOREST)
                ))
        ));

        addEffect(EffectSlot.STATIC, new KickerEffect("{G}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Kicked(),
                        new SearchLibraryEffect(landWithBasicLandType, LibrarySearchDestination.HAND)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "a land"),
                        new DrawCardEffect()
                ),
                "{R}, {T}, Sacrifice a land: Draw a card."
        ));
    }
}
