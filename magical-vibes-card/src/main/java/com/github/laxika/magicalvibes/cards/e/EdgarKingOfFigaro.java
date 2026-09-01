package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EdgarKingOfFigaroEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "FIN", collectorNumber = "51")
@CardRegistration(set = "FIN", collectorNumber = "436")
public class EdgarKingOfFigaro extends Card {

    public EdgarKingOfFigaro() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)));
        addEffect(EffectSlot.STATIC, new EdgarKingOfFigaroEffect());
    }
}
