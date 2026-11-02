package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "238")
public class WranglerOfTheDamned extends Card {

    public WranglerOfTheDamned() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new NotCondition(new ControllerCastSpellThisTurn(new CardTruePredicate(), true)),
                new CreateTokenEffect("Spirit", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of())));
    }
}
