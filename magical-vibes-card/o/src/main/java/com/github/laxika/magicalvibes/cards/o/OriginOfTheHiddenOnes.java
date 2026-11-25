package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "36")
public class OriginOfTheHiddenOnes extends Card {

    public OriginOfTheHiddenOnes() {
        addTrigger(Triggers.chapter(1), DealDamageEffect.toAnyTarget(4));
        addTrigger(Triggers.chapter(2), new CreateTokenEffect(
                2, "Assassin", 1, 1, CardColor.BLACK, List.of(CardSubtype.ASSASSIN),
                Set.of(Keyword.MENACE), Set.of()));
        addTrigger(Triggers.chapter(3), new RegisterGlobalTriggeredAbilityEffect(new TriggeredAbility(
                Triggers.creatureYouControlAttacks().withFilter(new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN)),
                new CreateTokensAttackingEffect(1, new CreateTokenEffect(
                        1, "Assassin", 1, 1, CardColor.BLACK, List.of(CardSubtype.ASSASSIN),
                        Set.of(Keyword.MENACE), true, false)))));
    }
}
