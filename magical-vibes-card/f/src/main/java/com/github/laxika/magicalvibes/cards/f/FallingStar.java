package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "LEG", collectorNumber = "145")
public class FallingStar extends Card {

    public FallingStar() {
        // "Falling Star deals 3 damage to each creature it lands on. Tap all creatures dealt damage by Falling Star."
        addSpellEffect(DealDamageEffect.toEachCreature(3));
        addSpellEffect(new TapPermanentsEffect(Subjects.thisWay(StepKind.DEAL_DAMAGE, new PermanentIsCreaturePredicate())));
    }
}
