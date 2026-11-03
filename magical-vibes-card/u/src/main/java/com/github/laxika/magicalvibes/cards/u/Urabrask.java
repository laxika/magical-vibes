package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TheGreatWork;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerCastThreeOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "169")
@CardRegistration(set = "MOM", collectorNumber = "299")
@CardRegistration(set = "MOM", collectorNumber = "341")
public class Urabrask extends Card {

    public Urabrask() {
        setBackFaceCard(new TheGreatWork());

        var instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        var opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        target(opponent).addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery),
                new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER),
                new AwardManaEffect(ManaColor.RED));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new ExileSelfAndReturnTransformedEffect()),
                "{R}: Exile Urabrask, then return it to the battlefield transformed under its owner's control. Activate only as a sorcery and only if you've cast three or more instant and/or sorcery spells this turn.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new ControllerCastThreeOrMoreSpellsThisTurn(instantOrSorcery),
                "Activate only if you've cast three or more instant and/or sorcery spells this turn"));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheGreatWork";
    }
}
