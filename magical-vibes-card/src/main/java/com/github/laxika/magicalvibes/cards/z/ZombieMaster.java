package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "162")
@CardRegistration(set = "5ED", collectorNumber = "207")
public class ZombieMaster extends Card {

    public ZombieMaster() {
        // Other Zombie creatures have swampwalk.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 0, Set.of(Keyword.SWAMPWALK), GrantScope.ALL_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ZOMBIE))));

        // Other Zombies have "{B}: Regenerate this permanent."
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()), "{B}: Regenerate this permanent."),
                GrantScope.ALL_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ZOMBIE))));
    }
}
