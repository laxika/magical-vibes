package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BloodbatSummoner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "138")
public class VoldarenBloodcaster extends Card {

    private static final CreateTokenEffect CREATE_BLOOD = CreateTokenEffect.ofBloodToken(1);

    private static final ConditionalEffect TRANSFORM_IF_FIVE_BLOOD = new ConditionalEffect(
            new ControlsPermanentCount(5, new PermanentAllOfPredicate(List.of(
                    new PermanentIsTokenPredicate(),
                    new PermanentHasSubtypePredicate(CardSubtype.BLOOD)
            ))),
            new TransformSelfEffect());

    public VoldarenBloodcaster() {
        BloodbatSummoner backFace = new BloodbatSummoner();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Whenever this creature or another nontoken creature you control dies, create a Blood token.
        // Ally-death watchers are already off the battlefield when collected, so ON_ALLY alone is
        // "another"; ON_DEATH covers this creature's own death.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, CREATE_BLOOD);
        addEffect(EffectSlot.ON_DEATH, CREATE_BLOOD);

        // Whenever you create a Blood token, if you control five or more Blood tokens, transform.
        // Blood tokens are artifacts — gated via TriggeringCardConditionalEffect on ally-artifact ETB.
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, new TriggeringCardConditionalEffect(
                new CardAllOfPredicate(List.of(
                        new CardIsTokenPredicate(),
                        new CardSubtypePredicate(CardSubtype.BLOOD)
                )),
                TRANSFORM_IF_FIVE_BLOOD));
    }

    @Override
    public String getBackFaceClassName() {
        return "BloodbatSummoner";
    }
}
