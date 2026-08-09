package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "UDS", collectorNumber = "85")
public class GoblinMarshal extends Card {

    public GoblinMarshal() {
        // When this creature enters or dies, create two 1/1 red Goblin creature tokens.
        CreateTokenEffect goblinTokens = new CreateTokenEffect(
                2, "Goblin", 1, 1, CardColor.RED,
                List.of(CardSubtype.GOBLIN), Set.of(), Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, goblinTokens);
        addEffect(EffectSlot.ON_DEATH, goblinTokens);

        // Echo {4}{R}{R}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{4}{R}{R}"),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
