package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentOwnedByPlayerToHandThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "38")
@CardRegistration(set = "DSC", collectorNumber = "65")
public class PhenomenonInvestigators extends Card {

    private static final String BELIEVE = "Believe";
    private static final String DOUBT = "Doubt";

    public PhenomenonInvestigators() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseModeOnEnterEffect(List.of(BELIEVE, DOUBT)));

        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new ConditionalEffect(new SourceHasChosenMode(BELIEVE),
                        new CreateTokenEffect(1, "Horror", 2, 2, CardColor.BLACK,
                                List.of(CardSubtype.HORROR), Set.of(), Set.of(CardType.ENCHANTMENT))));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new SourceHasChosenMode(DOUBT),
                        new MayEffect(
                                new ReturnPermanentOwnedByPlayerToHandThenEffect(
                                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                        new DrawCardEffect(),
                                        "nonland permanent"),
                                "Return a nonland permanent you own to your hand?")));
    }
}
