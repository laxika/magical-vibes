package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()))), Subjects.you()),
                new ConditionalEffect(HappenedThisWay.sacrificed(), new CreateTokenEffect("Wolf", 2, 2,
                        CardColor.GREEN, List.of(CardSubtype.WOLF), Set.of(), Set.of())
                        .withAmount(AmountThisWay.sacrificedToughness()))),
                "Sacrifice a nontoken creature to create Wolf tokens?"));
    }
}
