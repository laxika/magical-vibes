package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "77")
@CardRegistration(set = "HOP", collectorNumber = "35")
public class NoxiousGhoul extends Card {

    public NoxiousGhoul() {
        var nonZombieCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))));
        var debuff = new BoostAllCreaturesEffect(-1, -1, nonZombieCreature);

        // Whenever this creature or another Zombie enters, all non-Zombie creatures get -1/-1 until end of turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, debuff);
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.ZOMBIE), debuff));
    }
}
