package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "37")
public class Thunderheads extends Card {

    public Thunderheads() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{2}{U}")));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, 1, "Weird", 3, 3, CardColor.BLUE, null,
                List.of(CardSubtype.WEIRD), Set.of(Keyword.DEFENDER, Keyword.FLYING), Set.of(),
                false, false, Map.of(), List.of(), false, true, false, 0, Set.of()));
        addEffect(EffectSlot.ON_SELF_CAST, new ReplicateEffect("{2}{U}"));
    }
}
