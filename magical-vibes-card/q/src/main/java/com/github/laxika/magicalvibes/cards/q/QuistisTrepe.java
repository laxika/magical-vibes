package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "66")
@CardRegistration(set = "FIN", collectorNumber = "440")
public class QuistisTrepe extends Card {

    public QuistisTrepe() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CastTargetInstantOrSorceryFromGraveyardEffect(
                GraveyardSearchScope.ALL_GRAVEYARDS, false, true, true));
    }
}
