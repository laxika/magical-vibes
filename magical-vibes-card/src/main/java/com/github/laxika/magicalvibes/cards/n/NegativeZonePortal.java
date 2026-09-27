package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.SourceExiledCardsMatchingAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.PutRandomCardExiledWithSourceIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "111")
@CardRegistration(set = "MSC", collectorNumber = "445")
public class NegativeZonePortal extends Card {

    public NegativeZonePortal() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(ExileGraveyardCardWithConditionalBonusEffect.creatureCardDrawsAndTracksSource(
                        GraveyardSearchScope.OPPONENT_GRAVEYARD)),
                "{2}, {T}: Exile target card from an opponent's graveyard. If it's a creature card, draw a card."));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new SourceExiledCardsMatchingAtLeast(4, new CardTypePredicate(CardType.CREATURE)),
                new FlipCoinWinEffect(null, new SacrificeSelfThenEffect(
                        new PutRandomCardExiledWithSourceIntoOwnersHandEffect()))));
    }
}
