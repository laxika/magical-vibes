package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttached;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "72")
@CardRegistration(set = "FIC", collectorNumber = "124")
public class SummoningMateria extends Card {

    public SummoningMateria() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new ConditionalEffect(new SourceIsAttached(),
                new AllowCastFromTopOfLibraryEffect(Set.of(CardType.CREATURE))));
        addStaticEffect(new StaticBoostEffect(2, 2,
                Set.of(Keyword.VIGILANCE), GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.GREEN), GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
