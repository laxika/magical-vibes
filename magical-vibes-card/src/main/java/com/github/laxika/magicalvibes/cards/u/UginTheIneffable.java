package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "2")
public class UginTheIneffable extends Card {

    public UginTheIneffable() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardIsColorlessPredicate(), 2, CostModificationScope.SELF));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new ExileTopCardAndCreateTokenEffect(new CreateTokenEffect(
                        1, "Spirit", 2, 2, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of()
                ))),
                "+1: Exile the top card of your library face down and look at it. Create a 2/2 colorless Spirit creature token. When that token leaves the battlefield, put the exiled card into your hand."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DestroyTargetPermanentEffect()),
                "−3: Destroy target permanent that's one or more colors.",
                new PermanentPredicateTargetFilter(
                        new PermanentColorInPredicate(Set.of(
                                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN
                        )),
                        "Target must be a colored permanent"
                )
        ));
    }
}
