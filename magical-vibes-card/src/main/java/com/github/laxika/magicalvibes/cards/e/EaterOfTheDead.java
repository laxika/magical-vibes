package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "44")
public class EaterOfTheDead extends Card {

    public EaterOfTheDead() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(new ConditionalEffect(
                        new SourceIsTapped(),
                        SequenceEffect.of(
                                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD,
                                        new CardTypePredicate(CardType.CREATURE)),
                                new UntapPermanentsEffect(TapUntapScope.SOURCE_PERMANENT)))),
                "{0}: If this creature is tapped, exile target creature card from a graveyard and untap this creature."
        ));
    }
}
