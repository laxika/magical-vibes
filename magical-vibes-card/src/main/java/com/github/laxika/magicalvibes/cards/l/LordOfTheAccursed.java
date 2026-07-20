package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "99")
public class LordOfTheAccursed extends Card {

    public LordOfTheAccursed() {
        // Other Zombies you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));

        // {1}{B}, {T}: All Zombies gain menace until end of turn.
        addActivatedAbility(new ActivatedAbility(true, "{1}{B}",
                List.of(new GrantKeywordEffect(Keyword.MENACE, GrantScope.ALL_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))),
                "{1}{B}, {T}: All Zombies gain menace until end of turn."));
    }
}
