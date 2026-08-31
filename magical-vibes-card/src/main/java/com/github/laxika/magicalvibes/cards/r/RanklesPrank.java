package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "102")
public class RanklesPrank extends Card {

    public RanklesPrank() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each player discards two cards",
                        new DiscardEffect(2, DiscardRecipient.EACH_PLAYER)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each player loses 4 life",
                        new LoseLifeEffect(4, LoseLifeRecipient.EACH_PLAYER)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each player sacrifices two creatures of their choice",
                        new SacrificePermanentsEffect(2, new PermanentIsCreaturePredicate(),
                                SacrificeRecipient.EACH_PLAYER).withSimultaneousChoices())
        )));
    }
}
