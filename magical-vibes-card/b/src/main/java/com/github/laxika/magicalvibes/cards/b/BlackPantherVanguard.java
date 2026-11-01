package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "207")
public class BlackPantherVanguard extends Card {

    public BlackPantherVanguard() {
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.HERO),
                        new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Create a 1/1 white Soldier creature token.",
                                        CreateTokenEffect.whiteSoldier(1)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Creatures you control get +1/+1 until end of turn.",
                                        new BoostAllOwnCreaturesEffect(1, 1)))))));
    }
}
