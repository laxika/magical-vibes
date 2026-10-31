package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "144")
public class TheLastAgniKai extends Card {

    public TheLastAgniKai() {
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls());

        addSpellEffect(new FightTargetsEffect());
        addSpellEffect(new AwardPersistentManaEffect(ManaColor.RED, new EventValue()));
        addSpellEffect(new GrantStaticEffectToPlayerUntilEndOfTurnEffect(
                new PreventManaDrainEffect(ManaColor.RED)));
    }
}
