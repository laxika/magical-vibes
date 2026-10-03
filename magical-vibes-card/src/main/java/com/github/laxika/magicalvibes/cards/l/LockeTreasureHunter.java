package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.MugEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;

@CardRegistration(set = "FIC", collectorNumber = "87")
@CardRegistration(set = "FIC", collectorNumber = "177")
@CardRegistration(set = "FIC", collectorNumber = "475")
public class LockeTreasureHunter extends Card {

    public LockeTreasureHunter() {
        // Locke can't be blocked by creatures with greater power.
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentNotPredicate(new PermanentPowerAtMostSourcePowerPredicate())));

        // Mug — Each player mills a card. If a land card was milled this way, create a Treasure
        // token. Until end of turn, you may cast a spell from among those cards.
        addEffect(EffectSlot.ON_ATTACK, new MugEffect());
    }
}
