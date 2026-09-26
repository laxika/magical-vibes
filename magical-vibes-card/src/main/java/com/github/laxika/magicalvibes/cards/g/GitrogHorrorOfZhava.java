package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantActivatedAbilityToTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SeekCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "58")
public class GitrogHorrorOfZhava extends Card {

    public GitrogHorrorOfZhava() {
        // At the beginning of each combat, if Gitrog is untapped, any opponent may sacrifice a
        // nontoken creature. If they do, tap Gitrog, then seek a land card onto the battlefield tapped.
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(
                        new SourceUntapped(),
                        new AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect(
                                new SeekCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), true))));

        // Whenever a land enters under your control, it perpetually gains the activated ability.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new PerpetuallyGrantActivatedAbilityToTriggeringCardEffect(landSacrificeDrawAbility()));
    }

    private static ActivatedAbility landSacrificeDrawAbility() {
        return new ActivatedAbility(
                true,
                "{B}{G}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{B}{G}, {T}, Sacrifice this land: Draw a card."
        );
    }
}
