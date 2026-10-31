package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetColorMode;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingSourceKind;

import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "119")
@CardRegistration(set = "FDN", collectorNumber = "346")
@CardRegistration(set = "FDN", collectorNumber = "407")
@CardRegistration(set = "FDN", collectorNumber = "479")
public class ElendaSaintOfDusk extends Card {

    public ElendaSaintOfDusk() {
        removeKeyword(Keyword.HEXPROOF);
        // "Hexproof from instants": opponents' instant spells and abilities from instant sources can't target.
        addStaticEffect(TargetingRestrictionEffect.hexproofFromCardTypes(Set.of(CardType.INSTANT)));
        addStaticEffect(new TargetingRestrictionEffect(
                TargetingSourceKind.ABILITIES, true, Set.of(), TargetColorMode.ANY, Set.of(CardType.INSTANT)));
        // "As long as your life total is greater than your starting life total" and
        // "at least 10 greater than your starting life total" (relative to the format's starting life).
        addStaticEffect(new ConditionalEffect(
                new ControllerLifeAtLeast(1, true),
                new StaticBoostEffect(1, 1, Set.of(Keyword.MENACE), GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(
                new ControllerLifeAtLeast(10, true),
                new StaticBoostEffect(5, 5, GrantScope.SELF)));
    }
}
