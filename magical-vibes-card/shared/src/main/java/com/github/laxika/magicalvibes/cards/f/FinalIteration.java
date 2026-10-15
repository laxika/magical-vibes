package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

/**
 * Final Iteration — back face of Docent of Perfection.
 * Flying
 * Wizards you control get +2/+1 and have flying.
 * Whenever you cast an instant or sorcery spell, create a 1/1 blue Human Wizard creature token.
 */
public class FinalIteration extends Card {

    public FinalIteration() {
        // Wizards you control get +2/+1 and have flying.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 1, Set.of(Keyword.FLYING), GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.WIZARD)));

        // Whenever you cast an instant or sorcery spell, create a 1/1 blue Human Wizard creature token.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                List.of(new CreateTokenEffect("Human Wizard", 1, 1,
                        CardColor.BLUE,
                        List.of(CardSubtype.HUMAN, CardSubtype.WIZARD),
                        Set.of(), Set.of()))));
    }
}
