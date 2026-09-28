package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "548")
@CardRegistration(set = "MSC", collectorNumber = "851")
public class IronManTonyStark extends Card {

    public IronManTonyStark() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES,
                        new PermanentIsAttackingPredicate()));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.RED),
                List.of(new CreateTokenEffect("Robot", 2, 1, null,
                        List.of(CardSubtype.ROBOT, CardSubtype.HERO), Set.of(Keyword.FLYING),
                        Set.of(CardType.ARTIFACT)))));
    }
}
