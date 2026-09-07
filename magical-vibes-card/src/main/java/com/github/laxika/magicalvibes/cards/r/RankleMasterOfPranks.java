package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "101")
public class RankleMasterOfPranks extends Card {

    public RankleMasterOfPranks() {
        CardEffect discard = new DiscardEffect(1, DiscardRecipient.EACH_PLAYER);
        CardEffect loseLifeAndDraw = new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER);
        CardEffect draw = new EachPlayerDrawsCardEffect(1);
        CardEffect sacrifice = new SacrificePermanentsEffect(
                1, new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                SacrificeRecipient.EACH_PLAYER);

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ChooseOneEffect(List.of(
                option("Choose no modes"),
                option("Each player discards a card", discard),
                option("Each player loses 1 life and draws a card", loseLifeAndDraw, draw),
                option("Each player sacrifices a creature", sacrifice),
                option("Each player discards a card; each player loses 1 life and draws a card",
                        discard, loseLifeAndDraw, draw),
                option("Each player discards a card; each player sacrifices a creature", discard, sacrifice),
                option("Each player loses 1 life and draws a card; each player sacrifices a creature",
                        loseLifeAndDraw, draw, sacrifice),
                option("Each player discards a card; each player loses 1 life and draws a card; "
                        + "each player sacrifices a creature", discard, loseLifeAndDraw, draw, sacrifice)
        )));
    }

    private static ChooseOneEffect.ChooseOneOption option(String label, CardEffect... effects) {
        return new ChooseOneEffect.ChooseOneOption(label, List.of(effects));
    }
}
