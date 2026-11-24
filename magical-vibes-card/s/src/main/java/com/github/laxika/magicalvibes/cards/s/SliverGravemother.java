package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleEffect;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.EncoreEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToCardsEffect.PerCard;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "707")
@CardRegistration(set = "CMM", collectorNumber = "777")
@CardRegistration(set = "CMM", collectorNumber = "782")
public class SliverGravemother extends Card {

    public SliverGravemother() {
        addStaticEffect(new IgnoreLegendRuleEffect(
                Subjects.controlledBy(Subjects.you(), new PermanentHasSubtypePredicate(CardSubtype.SLIVER))));
        addStaticEffect(new GrantGraveyardAbilityToCardsEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.SLIVER))),
                PerCard.ENCORE_FOR_MANA_VALUE));
        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{5}",
                List.of(new ExileSelfFromGraveyardCost(), new EncoreEffect()),
                "Encore {5}",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
