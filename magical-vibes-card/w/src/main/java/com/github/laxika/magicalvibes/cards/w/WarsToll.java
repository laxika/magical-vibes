package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentCreaturesAttackTogetherEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "DIS", collectorNumber = "77")
public class WarsToll extends Card {

    public WarsToll() {
        // Whenever an opponent taps a land for mana, tap all lands that player controls.
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new TapPermanentsEffect(
                TapUntapScope.TARGET_PLAYERS_PERMANENTS, new PermanentIsLandPredicate()));

        // If a creature an opponent controls attacks, all creatures that opponent controls attack if able.
        addEffect(EffectSlot.STATIC, new OpponentCreaturesAttackTogetherEffect());
    }
}
