package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "NPH", collectorNumber = "8")
public class DueRespect extends Card {

    public DueRespect() {
        addSpellEffect(new GrantStaticEffectToPlayerEffect(
                EnterPermanentsOfTypesTappedEffect.matching(new PermanentTruePredicate(), Subjects.you()),
                Subjects.eachPlayer(), Duration.UNTIL_END_OF_TURN));
        addSpellEffect(new DrawCardEffect());
    }
}
