package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToCreatureControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "4")
public class BridesGown extends Card {

    public BridesGown() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AnyPlayerControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                        new PermanentNamedPredicate("Groom's Finery"),
                        new PermanentAttachedToCreatureControlledBySourceControllerPredicate()))),
                new StaticBoostEffect(0, 2, Set.of(Keyword.FIRST_STRIKE), GrantScope.EQUIPPED_CREATURE)));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
