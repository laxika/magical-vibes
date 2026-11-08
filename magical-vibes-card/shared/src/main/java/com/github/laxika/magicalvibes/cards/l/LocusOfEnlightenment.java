package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class LocusOfEnlightenment extends Card {

    public LocusOfEnlightenment() {
        addStaticEffect(new GainActivatedAbilitiesOfExiledCardsEffect(true));
        addTrigger(Triggers.youActivateNonManaAbility(),
                new CopyTriggeringSpellOrAbilityEffect());
    }
}
