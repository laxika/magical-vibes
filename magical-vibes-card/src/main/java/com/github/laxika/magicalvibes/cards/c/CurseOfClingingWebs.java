package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureCardAndCreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIC", collectorNumber = "25")
@CardRegistration(set = "MIC", collectorNumber = "63")
public class CurseOfClingingWebs extends Card {

    public CurseOfClingingWebs() {
        addEffect(EffectSlot.ON_ENCHANTED_PLAYER_NONTOKEN_CREATURE_DIES,
                new ExileDyingCreatureCardAndCreateTokenEffect(
                        new CreateTokenEffect(1, "Spider", 1, 2, CardColor.GREEN,
                                List.of(CardSubtype.SPIDER), Set.of(Keyword.REACH), Set.of())));
    }
}
