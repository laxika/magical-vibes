package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "149")
public class AnthousaSetessanHero extends Card {

    public AnthousaSetessanHero() {
        // Heroic — Whenever you cast a spell that targets Anthousa, up to three target lands you
        // control each become 2/2 Warrior creatures until end of turn. They're still lands.
        target(TargetFilters.landYouControl(), 0, 3).addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        null,
                        List.of(new AnimatePermanentsEffect(
                                2, 2, List.of(CardSubtype.WARRIOR), Set.of(), null, Set.of(),
                                GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN)),
                        new StackEntryTargetsSourcePredicate()));
    }
}
