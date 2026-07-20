package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AKH", collectorNumber = "183")
public class SandwurmConvergence extends Card {

    public SandwurmConvergence() {
        // Creatures with flying can't attack you or planeswalkers you control.
        // Exemption = creatures WITHOUT flying (they may still attack); flying creatures are barred.
        addEffect(EffectSlot.STATIC, new CreaturesCantAttackControllerUnlessPredicateEffect(
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)), true));

        // At the beginning of your end step, create a 5/5 green Wurm creature token.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new CreateTokenEffect(
                "Wurm", 5, 5, CardColor.GREEN, List.of(CardSubtype.WURM), Set.of(), Set.of()));
    }
}
