package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BloodbatSummoner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "138")
@CardRegistration(set = "VOW", collectorNumber = "137")
@CardRegistration(set = "DBL", collectorNumber = "404")
public class VoldarenBloodcaster extends Card {

    private static final CreateTokenEffect CREATE_BLOOD = CreateTokenEffect.ofBloodToken(1);

    private static final CardEffect TRANSFORM_IF_FIVE_BLOOD = new TransformSelfEffect();

    public VoldarenBloodcaster() {
        setBackFaceCard(new BloodbatSummoner());

        // Whenever this creature or another nontoken creature you control dies, create a Blood token.
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL), CREATE_BLOOD);

        // Whenever you create a Blood token, if you control five or more Blood tokens, transform.
        // Blood tokens are artifacts: the trigger's filter is "a Blood token" among them.
        var bloodToken = new PermanentAllOfPredicate(List.of(
                new PermanentIsTokenPredicate(), new PermanentHasSubtypePredicate(CardSubtype.BLOOD)));
        addTrigger(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.ARTIFACT, bloodToken)),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new ControlsPermanentCount(5, bloodToken)),
                TRANSFORM_IF_FIVE_BLOOD);
    }

    @Override
    public String getBackFaceClassName() {
        return "BloodbatSummoner";
    }
}
