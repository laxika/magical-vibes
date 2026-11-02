package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedPermanentSubtypeAtLeastThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "609")
@CardRegistration(set = "SLX", collectorNumber = "9")
public class HavengulLaboratory extends Card {

    public HavengulLaboratory() {
        setBackFaceCard(new HavengulMystery());

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {4}, {T}: Investigate.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(CreateTokenEffect.ofClueToken(1)),
                "{4}, {T}: Investigate."
        ));

        // At the beginning of your end step, if you sacrificed three or more Clues this turn,
        // transform Havengul Laboratory.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(
                        new ControllerSacrificedPermanentSubtypeAtLeastThisTurn(3, CardSubtype.CLUE),
                        new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "HavengulMystery";
    }
}
