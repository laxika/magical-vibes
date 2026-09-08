package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "106")
public class ImmolationShaman extends Card {

    public ImmolationShaman() {
        addEffect(EffectSlot.ON_OPPONENT_ACTIVATES_NONMANA_ABILITY, new TriggeringPermanentConditionalEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate())),
                new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{R}",
                List.of(
                        new BoostSelfEffect(3, 3),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)),
                "{3}{R}{R}: This creature gets +3/+3 and gains menace until end of turn."
        ));
    }
}
