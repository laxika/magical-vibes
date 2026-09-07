package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureThenLoseLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "183")
public class DireTactics extends Card {

    public DireTactics() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new ExileTargetCreatureThenLoseLifeEqualToToughnessEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
    }
}
