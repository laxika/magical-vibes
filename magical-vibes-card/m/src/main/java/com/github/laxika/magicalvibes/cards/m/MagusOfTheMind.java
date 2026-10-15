package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

import java.util.List;

@CardRegistration(set = "C17", collectorNumber = "12")
public class MagusOfTheMind extends Card {

    public MagusOfTheMind() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(
                        new SacrificeSelfCost(),
                        new ShuffleLibraryEffect(false),
                        new ExileTopCardMayPlayThisTurnEffect(
                                new Sum(new Fixed(1), new SpellsCastThisTurn(CountScope.ANY_PLAYER)),
                                true)
                ),
                "{U}, {T}, Sacrifice this creature: Shuffle your library, then exile the top X cards, where X is one plus the number of spells cast this turn. Until end of turn, you may play lands and cast spells from among cards exiled this way without paying their mana costs."
        ));
    }
}
