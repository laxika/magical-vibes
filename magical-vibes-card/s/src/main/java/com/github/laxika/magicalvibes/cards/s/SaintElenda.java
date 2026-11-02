package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YOTJ", collectorNumber = "4")
public class SaintElenda extends Card {

    public SaintElenda() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), DraftFromSpellbookEffect.conjureIntoHand(
                List.of(
                        new DraftFromSpellbookEffect.SpellbookCard("M21", "17"),
                        new DraftFromSpellbookEffect.SpellbookCard("DOM", "22"),
                        new DraftFromSpellbookEffect.SpellbookCard("XLN", "32"),
                        new DraftFromSpellbookEffect.SpellbookCard("XLN", "16")
                ),
                DraftFromSpellbookEffect.DraftMode.MAY_CAST_WITHOUT_PAYING_MANA_COST_TO_CONTROLLER));

        LifeGainedThisTurn lifeGained = new LifeGainedThisTurn(CountScope.CONTROLLER);
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new GainedLifeThisTurn(),
                new CreateTokenEffect("Avatar", lifeGained, lifeGained, CardColor.WHITE,
                        List.of(CardSubtype.AVATAR), Set.of(), Set.of())));
    }
}
