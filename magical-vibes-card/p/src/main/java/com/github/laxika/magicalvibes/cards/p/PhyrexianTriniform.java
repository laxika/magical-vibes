package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSourceAttackingOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "370")
public class PhyrexianTriniform extends Card {

    public PhyrexianTriniform() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                3, "Phyrexian Golem", 3, 3, null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT)));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{12}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopiesOfSourceAttackingOpponentsEffect()
                ),
                "Encore {12} ({12}, Exile this card from your graveyard: For each opponent, create a token copy "
                        + "that attacks that opponent this turn if able. They gain haste. Sacrifice them at the beginning "
                        + "of the next end step. Activate only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
