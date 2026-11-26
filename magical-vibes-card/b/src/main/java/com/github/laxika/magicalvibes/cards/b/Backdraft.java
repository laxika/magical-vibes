package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DamageDealtByTargetPlayerSorceryThisTurn;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSorceryCasterForBackdraftEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "LEG", collectorNumber = "132")
public class Backdraft extends Card {

    public Backdraft() {
        addSpellEffect(new ChooseSorceryCasterForBackdraftEffect());
        addSpellEffect(DealDamageEffect.to(Subjects.chosenPlayer(),
                new Divided(new DamageDealtByTargetPlayerSorceryThisTurn(), 2)));
    }
}
