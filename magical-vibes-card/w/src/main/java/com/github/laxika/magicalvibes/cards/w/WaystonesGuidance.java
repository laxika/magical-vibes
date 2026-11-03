package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "3")
public class WaystonesGuidance extends Card {

    public WaystonesGuidance() {
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsTokenPredicate(),
                        new PermanentIsAttackingPredicate()))));

        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)).nthThisTurn(1),
                new GrantTriggeredAbilityToCastSpellEffect(
                        new TriggeredAbility(Triggers.attacks(),
                        SequenceEffect.of(
                                new CreateTokenEffect(1, "Warrior", 1, 1, CardColor.RED,
                                        List.of(CardSubtype.WARRIOR), true),
                                new SacrificeCreatedPermanentsAtEndStepEffect()))));
    }
}
