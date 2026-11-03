package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "4")
@CardRegistration(set = "WOC", collectorNumber = "37")
public class GylwainCastingDirector extends Card {

    public GylwainCastingDirector() {
        ChooseOneEffect choice = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a Royal Role token attached to that creature.",
                        new CreateTokenAttachedToTriggeringPermanentEffect(royalRoleToken())),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a Sorcerer Role token attached to that creature.",
                        new CreateTokenAttachedToTriggeringPermanentEffect(sorcererRoleToken())),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a Monster Role token attached to that creature.",
                        new CreateTokenAttachedToTriggeringPermanentEffect(monsterRoleToken()))));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new ChooseOneAtTriggerTimeEffect(choice));
    }

    private static CreateTokenEffect royalRoleToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Royal",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.AURA, CardSubtype.ROLE),
                Set.of(),
                Set.of(),
                false,
                false,
                List.of(
                        StaticAbility.of(SequenceEffect.of(
                        new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE),
                        new GrantTriggeredAbilityEffect(
                                new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                                new CounterUnlessPaysEffect(1)),
                                GrantScope.ENCHANTED_CREATURE)))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }

    private static CreateTokenEffect sorcererRoleToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Sorcerer",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.AURA, CardSubtype.ROLE),
                Set.of(),
                Set.of(),
                false,
                false,
                List.of(
                        StaticAbility.of(SequenceEffect.of(
                        new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE),
                        new GrantTriggeredAbilityEffect(
                                new TriggeredAbility(Triggers.attacks(),
                                new ScryEffect(1)),
                                GrantScope.ENCHANTED_CREATURE)))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }

    private static CreateTokenEffect monsterRoleToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Monster",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.AURA, CardSubtype.ROLE),
                Set.of(),
                Set.of(),
                false,
                false,
                List.of(
                        StaticAbility.of(SequenceEffect.of(
                        new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ENCHANTED_CREATURE)))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
