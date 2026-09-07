package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "2")
public class BelfrySpirit extends Card {

    public BelfrySpirit() {
        // When this creature enters or the creature it haunts dies, create two 1/1 black Bat
        // creature tokens with flying.
        CreateTokenEffect bats = new CreateTokenEffect(2, "Bat", 1, 1,
                CardColor.BLACK, List.of(CardSubtype.BAT), Set.of(Keyword.FLYING), Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, bats);
        addEffect(EffectSlot.ON_HAUNTED_CREATURE_DIES, bats);

        // Haunt
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_DEATH, new HauntEffect());
    }
}
