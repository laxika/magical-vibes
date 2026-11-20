package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DOM", collectorNumber = "50")
public class DeepFreeze extends Card {

    public DeepFreeze() {
        target(TargetFilters.creature())
        // Enchanted creature has base power and toughness 0/4
        .addStaticEffect(new SetBasePowerToughnessEffect(0, 4, Subjects.attachedCreature()))
        // Enchanted creature has defender
        .addStaticEffect(new GrantKeywordEffect(Keyword.DEFENDER, Subjects.attachedCreature()))
        // Enchanted creature loses all other abilities
        .addStaticEffect(new LosesAllAbilitiesEffect(Subjects.attachedCreature()))
        // Enchanted creature is a blue Wall in addition to its other colors and types
        .addStaticEffect(new GrantColorEffect(CardColor.BLUE, Subjects.attachedCreature()))
        .addStaticEffect(new GrantSubtypeEffect(CardSubtype.WALL, Subjects.attachedCreature()));
    }
}
