package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.amount.PlayersWhoDiscardedThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "101")
@CardRegistration(set = "TLE", collectorNumber = "184")
public class AzulaRuthlessFirebender extends Card {

    public AzulaRuthlessFirebender() {
        addEffect(EffectSlot.ON_ATTACK,
                new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 1));
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new MayEffect(new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                        "Discard a card?"),
                new ExperienceCountersEffect(new PlayersWhoDiscardedThisTurn())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new BoostSelfEffect(new ControllerExperienceCounters(),
                                new ControllerExperienceCounters()),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)
                ),
                "{2}{B}: Until end of turn, Azula gets +1/+1 for each experience counter you have and gains menace."
        ));
    }
}
