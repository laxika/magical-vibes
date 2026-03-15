package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

/**
 * Lord of Lineage — back face of Bloodline Keeper.
 * 5/5 Vampire, Flying.
 * Other Vampire creatures you control get +2/+2.
 * {T}: Create a 2/2 black Vampire creature token with flying.
 */
public class LordOfLineage extends Card {

    public LordOfLineage() {
        // Other Vampire creatures you control get +2/+2.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.VAMPIRE))));

        // {T}: Create a 2/2 black Vampire creature token with flying.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new CreateCreatureTokenEffect(
                        "Vampire", 2, 2,
                        CardColor.BLACK,
                        List.of(CardSubtype.VAMPIRE),
                        Set.of(Keyword.FLYING),
                        Set.of()
                )),
                "{T}: Create a 2/2 black Vampire creature token with flying."
        ));
    }
}
