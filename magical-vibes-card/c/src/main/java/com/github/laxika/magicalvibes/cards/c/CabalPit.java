package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "315")
public class CabalPit extends Card {

    public CabalPit() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), new DealDamageToPlayersEffect(1, Subjects.you())),
                "{T}: Add {B}. This land deals 1 damage to you."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new SacrificeSelfCost(), BoostEffect.target(-2, -2)),
                "{B}, {T}, Sacrifice this land: Target creature gets -2/-2 until end of turn. Activate only if there are seven or more cards in your graveyard.",
                TargetFilters.creature()
        ).withRequiredGraveyardCards(new CardTruePredicate(), 7, "cards in your graveyard"));
    }
}
