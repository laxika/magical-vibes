package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "63")
public class ShaoJun extends Card {

    public ShaoJun() {
        // During your turn, Shao Jun has flying and first strike.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerTurn(),
                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE), GrantScope.SELF)));

        // Tap two untapped artifacts you control: Shao Jun deals 1 damage to each opponent.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsArtifactPredicate()),
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)
                ),
                "Tap two untapped artifacts you control: Shao Jun deals 1 damage to each opponent."
        ));
    }
}
