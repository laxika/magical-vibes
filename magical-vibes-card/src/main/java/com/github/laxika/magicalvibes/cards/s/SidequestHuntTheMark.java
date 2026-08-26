package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.y.YiazmatUltimateMark;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.CreatureDiedUnderOpponentControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "119")
public class SidequestHuntTheMark extends Card {

    public SidequestHuntTheMark() {
        setBackFaceCard(new YiazmatUltimateMark());

        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new CreatureDiedUnderOpponentControlThisTurn(),
                        SequenceEffect.of(
                                CreateTokenEffect.ofTreasureToken(1),
                                new ConditionalEffect(
                                        new ControlsPermanentCount(3,
                                                new PermanentHasSubtypePredicate(CardSubtype.TREASURE)),
                                        new TransformSelfEffect()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "YiazmatUltimateMark";
    }
}
