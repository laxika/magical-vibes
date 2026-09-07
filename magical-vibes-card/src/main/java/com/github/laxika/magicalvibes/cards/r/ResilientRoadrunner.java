package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "141")
public class ResilientRoadrunner extends Card {

    public ResilientRoadrunner() {
        addEffect(EffectSlot.STATIC, new ProtectionFromSubtypesEffect(Set.of(CardSubtype.COYOTE)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentHasKeywordPredicate(Keyword.HASTE), "creatures with haste", true)),
                "{3}: This creature can't be blocked this turn except by creatures with haste."
        ));
    }
}
