package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSourceAttackingOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ECC", collectorNumber = "6")
@CardRegistration(set = "ECC", collectorNumber = "26")
public class Subterfuge extends Card {

    public Subterfuge() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantEffectToTargetUntilEndOfTurnEffect(
                                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                new DrawCardEffect(new EventValue())));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{7}{U}{U}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopiesOfSourceAttackingOpponentsEffect()
                ),
                "Encore {7}{U}{U} ({7}{U}{U}, Exile this card from your graveyard: For each opponent, create a token copy "
                        + "that attacks that opponent this turn if able. They gain haste. Sacrifice them at the beginning "
                        + "of the next end step. Activate only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
