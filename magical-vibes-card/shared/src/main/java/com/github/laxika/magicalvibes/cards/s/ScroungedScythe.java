package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

/** Back face of {@link com.github.laxika.magicalvibes.cards.h.HarvestHand}. */
public class ScroungedScythe extends Card {

    public ScroungedScythe() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantKeywordEffect(Keyword.MENACE, GrantScope.EQUIPPED_CREATURE,
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
