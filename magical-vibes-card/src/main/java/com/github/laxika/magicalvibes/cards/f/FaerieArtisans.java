package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentThenExileOtherTokensEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "92")
@CardRegistration(set = "CMM", collectorNumber = "488")
public class FaerieArtisans extends Card {

    public FaerieArtisans() {
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardIsTokenPredicate()),
                        new CreateTokenCopyOfTargetPermanentThenExileOtherTokensEffect(
                                Set.of(CardType.ARTIFACT))));
    }
}
