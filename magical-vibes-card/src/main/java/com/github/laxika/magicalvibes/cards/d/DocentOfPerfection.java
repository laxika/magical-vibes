package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FinalIteration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "62")
public class DocentOfPerfection extends Card {

    public DocentOfPerfection() {
        FinalIteration backFace = new FinalIteration();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Whenever you cast an instant or sorcery spell, create a 1/1 blue Human Wizard
        // creature token. Then if you control three or more Wizards, transform this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                List.of(
                        new CreateTokenEffect("Human Wizard", 1, 1,
                                CardColor.BLUE,
                                List.of(CardSubtype.HUMAN, CardSubtype.WIZARD),
                                Set.of(), Set.of()),
                        new ConditionalEffect(
                                new ControlsPermanentCount(3, new PermanentHasSubtypePredicate(CardSubtype.WIZARD)),
                                new TransformSelfEffect()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "FinalIteration";
    }
}
