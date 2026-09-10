package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "260")
public class TempleOfTheDragonQueen extends Card {

    public TempleOfTheDragonQueen() {
        PermanentHasAnySubtypePredicate dragon = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.DRAGON));

        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(0, dragon),
                new RevealSubtypeOrEntersTappedEffect(CardSubtype.DRAGON)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardChosenColorManaEffect()),
                "{T}: Add one mana of the chosen color."
        ));
    }
}
