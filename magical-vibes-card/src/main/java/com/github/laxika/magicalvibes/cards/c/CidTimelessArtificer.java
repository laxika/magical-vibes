package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "216")
public class CidTimelessArtificer extends Card {

    public CidTimelessArtificer() {
        PermanentCount artificersYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ARTIFICER), CountScope.CONTROLLER);
        CardsInGraveyard artificersInYourGraveyard = new CardsInGraveyard(
                new CardSubtypePredicate(CardSubtype.ARTIFICER), CountScope.CONTROLLER);
        Sum boost = new Sum(artificersYouControl, artificersInYourGraveyard);

        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                boost, boost, GrantScope.OWN_CREATURES,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.HERO)))));
        addCycling("{W}{U}");
    }
}
