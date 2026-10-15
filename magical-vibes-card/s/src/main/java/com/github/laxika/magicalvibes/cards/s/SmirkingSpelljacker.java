package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;

@CardRegistration(set = "OTC", collectorNumber = "16")
@CardRegistration(set = "OTC", collectorNumber = "52")
public class SmirkingSpelljacker extends Card {

    public SmirkingSpelljacker() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(new StackEntryControlledByPredicate()),
                "Target must be a spell an opponent controls."))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetSpellUntilSourceLeavesEffect());

        addEffect(EffectSlot.ON_ATTACK, new MayCastCardExiledWithSourceEffect());
    }
}
