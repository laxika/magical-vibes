package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "117")
public class PugnaciousPugilist extends Card {

    public PugnaciousPugilist() {
        addCastingOption(AlternateHandCast.blitz("{3}{R}"));

        List<Ability> devilTokenEffects =
                List.of(new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), new DealDamageToAnyTargetEffect(1)));
        addTrigger(Triggers.attacks(), new CreateTokenEffect(
                CardType.CREATURE, 1, "Devil", 1, 1, CardColor.RED, null,
                List.of(CardSubtype.DEVIL), Set.of(), Set.of(), true, false,
                devilTokenEffects, List.of(), false, false, false, 0, Set.of()));

        addStaticEffect(new ConditionalEffect(
                new CastForAlternateCost(), new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new CastForAlternateCost()),
                new DrawCardEffect(1));
    }
}
