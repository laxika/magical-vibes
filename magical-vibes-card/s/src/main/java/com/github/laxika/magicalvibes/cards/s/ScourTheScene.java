package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YMKM", collectorNumber = "27")
public class ScourTheScene extends Card {

    public ScourTheScene() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect(1, "Detective", 2, 2, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.DETECTIVE)));
        addTrigger(Triggers.sacrificed(new PermanentIsArtifactPredicate(), ObjectRelation.YOU_CONTROL),
                new ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffect(1, 0));
    }
}
