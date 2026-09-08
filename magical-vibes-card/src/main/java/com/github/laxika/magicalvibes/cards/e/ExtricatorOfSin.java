package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "23")
public class ExtricatorOfSin extends Card {

    public ExtricatorOfSin() {
        setBackFaceCard(new ExtricatorOfFlesh());

        // When this creature enters, you may sacrifice another permanent. If you do, create a
        // 3/2 colorless Eldrazi Horror creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        eldraziHorrorToken(),
                        "another permanent"),
                "Sacrifice another permanent?"));

        // Delirium — At the beginning of your upkeep, if there are four or more card types among
        // cards in your graveyard, transform this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new Delirium(), new TransformSelfEffect()));
    }

    static CreateTokenEffect eldraziHorrorToken() {
        return new CreateTokenEffect(
                1, "Eldrazi Horror", 3, 2, null,
                List.of(CardSubtype.ELDRAZI, CardSubtype.HORROR), false);
    }

    @Override
    public String getBackFaceClassName() {
        return "ExtricatorOfFlesh";
    }
}
