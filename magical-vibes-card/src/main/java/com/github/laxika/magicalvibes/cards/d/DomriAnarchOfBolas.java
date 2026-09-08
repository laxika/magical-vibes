package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerCreatureSpellsCantBeCounteredThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "191")
public class DomriAnarchOfBolas extends Card {

    public DomriAnarchOfBolas() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN)),
                        new GrantControllerCreatureSpellsCantBeCounteredThisTurnEffect()),
                "+1: Add {R} or {G}. Creature spells you cast this turn can't be countered."
        ));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new FightTargetsEffect()),
                "−2: Target creature you control fights target creature you don't control.",
                null, -2, null, null,
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls()),
                2, 2
        ));
    }
}
