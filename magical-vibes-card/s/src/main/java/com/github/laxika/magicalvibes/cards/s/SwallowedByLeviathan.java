package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "FIN", collectorNumber = "79")
public class SwallowedByLeviathan extends Card {

    public SwallowedByLeviathan() {
        addSpellEffect(new SurveilEffect(2));
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(new CardsInGraveyard(null, CountScope.CONTROLLER)));
    }
}
