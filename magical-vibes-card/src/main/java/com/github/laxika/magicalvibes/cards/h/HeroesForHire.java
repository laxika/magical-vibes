package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "688")
public class HeroesForHire extends Card {

    public HeroesForHire() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofTreasureToken(3));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.TREASURE),
                                "Sacrifice a Treasure"),
                        new ExileTopCardMayPlayThisTurnEffect(false)
                ),
                "Sacrifice a Treasure: Exile the top card of your library. You may play it this turn."
        ));
    }
}
