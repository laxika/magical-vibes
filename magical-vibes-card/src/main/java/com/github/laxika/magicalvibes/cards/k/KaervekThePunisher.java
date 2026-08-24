package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.CardColor;

@CardRegistration(set = "OTJ", collectorNumber = "92")
public class KaervekThePunisher extends Card {

    public KaervekThePunisher() {
        addEffect(EffectSlot.ON_CONTROLLER_COMMITS_CRIME,
                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                        new CardColorPredicate(CardColor.BLACK),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        2));
    }
}
