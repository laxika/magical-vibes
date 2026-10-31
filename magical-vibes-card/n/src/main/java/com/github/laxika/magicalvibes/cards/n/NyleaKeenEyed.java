package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCardTypeFromAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandElseMayGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "185")
@CardRegistration(set = "THB", collectorNumber = "266")
public class NyleaKeenEyed extends Card {

    public NyleaKeenEyed() {
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new DevotionToColorAtLeast(ManaColor.GREEN, 5)),
                new RemoveCardTypeFromAttachedPermanentEffect(CardType.CREATURE, GrantScope.SELF)));
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.CREATURE), 1, CostModificationScope.SELF));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new RevealTopCardMatchingToHandElseMayGraveyardEffect(
                        new CardTypePredicate(CardType.CREATURE))),
                "{2}{G}: Reveal the top card of your library. If it's a creature card, put it into "
                        + "your hand. Otherwise, you may put it into your graveyard."));
    }
}
