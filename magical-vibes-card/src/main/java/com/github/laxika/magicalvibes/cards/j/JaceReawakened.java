package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerOwnTurnCountAtMost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandAndPlotEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "271")
public class JaceReawakened extends Card {

    public JaceReawakened() {
        setCastCondition(new NotCondition(new ControllerOwnTurnCountAtMost(3)));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "+1: Draw a card, then discard a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new MayEffect(
                        new ExileCardFromHandAndPlotEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                                        new CardMaxManaValuePredicate(3))),
                                "a nonland card with mana value 3 or less"),
                        "You may exile a nonland card with mana value 3 or less from your hand and plot it.")),
                "+1: You may exile a nonland card with mana value 3 or less from your hand. If you do, it becomes plotted."
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                        EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                        new CopyControllerCastSpellOnSpellCastEffect(new CardTruePredicate(), null, null))),
                "-6: Until end of turn, whenever you cast a spell, copy it. You may choose new targets for the copy."
        ));
    }
}
