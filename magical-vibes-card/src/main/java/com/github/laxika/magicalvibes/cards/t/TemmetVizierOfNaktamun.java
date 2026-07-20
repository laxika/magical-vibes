package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "207")
public class TemmetVizierOfNaktamun extends Card {

    public TemmetVizierOfNaktamun() {
        // At the beginning of combat on your turn, target creature token you control gets +1/+1
        // until end of turn and can't be blocked this turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTokenPredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "Target must be a creature token you control"
        ))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new BoostTargetCreatureEffect(1, 1))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new MakeCreatureUnblockableEffect());

        // Embalm {3}{W}{U} ({3}{W}{U}, Exile this card from your graveyard: Create a token that's a
        // copy of it, except it's a white Zombie Human Cleric with no mana cost. Embalm only as a
        // sorcery.)
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{U}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.WHITE, CardSubtype.ZOMBIE, true)
                ),
                "Embalm {3}{W}{U} ({3}{W}{U}, Exile this card from your graveyard: Create a token that's a copy "
                        + "of it, except it's a white Zombie Human Cleric with no mana cost. Embalm only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
