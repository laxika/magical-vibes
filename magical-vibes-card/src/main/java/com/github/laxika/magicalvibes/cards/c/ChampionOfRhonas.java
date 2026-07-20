package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "AKH", collectorNumber = "159")
public class ChampionOfRhonas extends Card {

    public ChampionOfRhonas() {
        // Exert: "You may exert this creature as it attacks. When you do, you may put a creature card
        // from your hand onto the battlefield." Modeled as an optional attack trigger (matching
        // Glorybringer): the outer MayEffect is the exert decision, which skips the next untap.
        // The declinable hand-card choice covers the inner "you may put".
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF),
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature")
                ),
                "Exert Champion of Rhonas as it attacks? (You may put a creature card from your hand onto the battlefield.)"
        ));
    }
}
