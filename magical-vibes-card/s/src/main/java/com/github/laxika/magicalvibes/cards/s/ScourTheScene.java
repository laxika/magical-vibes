package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YMKM", collectorNumber = "27")
public class ScourTheScene extends Card {

    public ScourTheScene() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(1, "Detective", 2, 2, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.DETECTIVE)));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsArtifactPredicate(),
                        new ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffect(1, 0)));
    }
}
