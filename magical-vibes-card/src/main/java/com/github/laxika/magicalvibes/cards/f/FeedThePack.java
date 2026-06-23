package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureToCreateTokensEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

/**
 * Feed the Pack — {5}{G} Enchantment
 *
 * At the beginning of your end step, you may sacrifice a nontoken creature. If you do,
 * create X 2/2 green Wolf creature tokens, where X is the sacrificed creature's toughness.
 */
@CardRegistration(set = "DKA", collectorNumber = "114")
public class FeedThePack extends Card {

    public FeedThePack() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new MayEffect(
                new SacrificeCreatureToCreateTokensEqualToToughnessEffect(
                        new CreateTokenEffect("Wolf", 2, 2,
                                CardColor.GREEN, List.of(CardSubtype.WOLF), Set.of(), Set.of()),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())),
                "Sacrifice a nontoken creature to create Wolf tokens?"));
    }
}
