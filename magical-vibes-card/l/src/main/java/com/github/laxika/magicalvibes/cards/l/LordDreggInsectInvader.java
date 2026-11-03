package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "65")
public class LordDreggInsectInvader extends Card {

    public LordDreggInsectInvader() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new PermanentLeftBattlefieldUnderYourControlThisTurn()),
                new CreateTokenEffect(
                        "Insect Warrior", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.INSECT, CardSubtype.WARRIOR), Set.of(Keyword.FLYING), Set.of()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsTokenPredicate(), "Sacrifice a token", false),
                        new DrawCardEffect(1)),
                "{3}{G}, Sacrifice a token: Draw a card."
        ));
    }
}
