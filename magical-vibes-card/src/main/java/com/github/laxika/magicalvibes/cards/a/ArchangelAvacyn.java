package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterTransformSourceAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "INR", collectorNumber = "11")
public class ArchangelAvacyn extends Card {

    public ArchangelAvacyn() {
        AvacynThePurifier backFace = new AvacynThePurifier();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // When Archangel Avacyn enters, creatures you control gain indestructible until end of turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES));

        // When a non-Angel creature you control dies, transform Archangel Avacyn at the beginning
        // of the next upkeep.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.ANGEL)),
                new RegisterTransformSourceAtNextUpkeepEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "AvacynThePurifier";
    }
}
