package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterTransformSourceAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "11")
@CardRegistration(set = "INR", collectorNumber = "449")
@CardRegistration(set = "SOI", collectorNumber = "5")
@CardRegistration(set = "SLD", collectorNumber = "1156")
@CardRegistration(set = "SLD", collectorNumber = "1209")
@CardRegistration(set = "V17", collectorNumber = "1")
@CardRegistration(set = "SIR", collectorNumber = "13")
public class ArchangelAvacyn extends Card {

    public ArchangelAvacyn() {
        setBackFaceCard(new AvacynThePurifier());

        // When Archangel Avacyn enters, creatures you control gain indestructible until end of turn.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ALL_OWN_CREATURES));

        // When a non-Angel creature you control dies, transform Archangel Avacyn at the beginning
        // of the next upkeep.
        addTrigger(Triggers.creatureDies(
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ANGEL)),
                        ObjectRelation.YOU_CONTROL),
                new RegisterTransformSourceAtNextUpkeepEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "AvacynThePurifier";
    }
}
