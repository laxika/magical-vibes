package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "RAV", collectorNumber = "253")
@CardRegistration(set = "SLD", collectorNumber = "414")
public class ShadowOfDoubt extends Card {

    public ShadowOfDoubt() {
        addSpellEffect(new GrantStaticEffectToPlayerEffect(
                new CantSearchLibrariesEffect(Subjects.you(), false), Subjects.eachPlayer(), Duration.UNTIL_END_OF_TURN));
        addSpellEffect(new DrawCardEffect(1));
    }
}
