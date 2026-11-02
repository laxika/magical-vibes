package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "132")
@CardRegistration(set = "MSC", collectorNumber = "305")
public class FolkHero extends Card {

    public FolkHero() {
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.youCastSpell(),
                OncePerTurnTriggerEffect.perGrant(new SpellCastTriggerEffect(
                        new CardSharesCreatureTypeWithSourcePredicate(),
                        List.of(new DrawCardEffect(1))))),
                GrantScope.ALL_CREATURES,
                new PermanentIsCommanderPredicate(true)));
    }
}
