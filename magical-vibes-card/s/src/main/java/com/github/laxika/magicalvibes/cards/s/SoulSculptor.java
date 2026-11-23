package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "53")
public class SoulSculptor extends Card {

    public SoulSculptor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(
                        new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), Subjects.target(),
                                Duration.UNTIL_CREATURE_SPELL_CAST),
                        new LosesAllAbilitiesEffect(Subjects.target(), Duration.UNTIL_CREATURE_SPELL_CAST)),
                "{1}{W}, {T}: Target creature becomes an enchantment and loses all abilities until a player casts a creature spell.",
                TargetFilters.creature()
        ));
    }
}
