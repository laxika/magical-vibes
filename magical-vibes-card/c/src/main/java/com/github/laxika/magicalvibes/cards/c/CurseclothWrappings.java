package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantedCardAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "81")
public class CurseclothWrappings extends Card {

    public CurseclothWrappings() {
        // Zombies you control get +1/+1.
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.otherCreaturesYouControl(),
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ZOMBIE))));

        // {T}: Target creature card in your graveyard gains embalm until end of turn. The embalm cost is equal to its mana cost.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantFlashbackToTargetGraveyardCardEffect(
                        Set.of(CardType.CREATURE), GrantedCardAbilityEffect.Ability.EMBALM)),
                "{T}: Target creature card in your graveyard gains embalm until end of turn. The embalm cost is equal to its mana cost."
        ));
    }
}
