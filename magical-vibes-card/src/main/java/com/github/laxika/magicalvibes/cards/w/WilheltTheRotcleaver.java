package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "862")
@CardRegistration(set = "SLD", collectorNumber = "2285")
public class WilheltTheRotcleaver extends Card {

    public WilheltTheRotcleaver() {
        // Whenever another Zombie you control dies, if it didn't have decayed, create a 2/2 black
        // Zombie creature token with decayed.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.ZOMBIE),
                                new CardNotPredicate(new CardKeywordPredicate(Keyword.DECAYED))
                        )),
                        CreateTokenEffect.blackZombieWithDecayed(1)));

        // At the beginning of your end step, you may sacrifice a Zombie. If you do, draw a card.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                        new DrawCardEffect(),
                        "a Zombie"),
                "Sacrifice a Zombie to draw a card?"));
    }
}
