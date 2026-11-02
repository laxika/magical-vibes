package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "235")
public class WindcragSiege extends Card {

    public WindcragSiege() {
        addStaticEffect(new ChooseModeOnEnterEffect(List.of("Mardu", "Jeskai")));

        addStaticEffect(AdditionalTriggeredAbilityEffect.forAttackTriggers(
                        new PermanentTruePredicate(), new SourceHasChosenMode("Mardu")));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new SourceHasChosenMode("Jeskai"),
                new CreateTokenEffect(
                        1, "Goblin", 1, 1, CardColor.RED, Set.of(CardColor.RED),
                        List.of(CardSubtype.GOBLIN), Set.of(), Set.of(Keyword.LIFELINK, Keyword.HASTE))));
    }
}
