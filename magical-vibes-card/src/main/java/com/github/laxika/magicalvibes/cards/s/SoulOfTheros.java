package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "34")
public class SoulOfTheros extends Card {

    public SoulOfTheros() {
        List<CardEffect> abilityEffects = List.of(
                new BoostAllOwnCreaturesEffect(2, 2),
                new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.LIFELINK), GrantScope.OWN_CREATURES),
                new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.LIFELINK), GrantScope.SELF)
        );

        // {4}{W}{W}: Creatures you control get +2/+2 and gain first strike and lifelink until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}{W}",
                abilityEffects,
                "{4}{W}{W}: Creatures you control get +2/+2 and gain first strike and lifelink until end of turn."
        ));

        // {4}{W}{W}, Exile this card from your graveyard: Creatures you control get +2/+2 and gain
        // first strike and lifelink until end of turn.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new BoostAllOwnCreaturesEffect(2, 2),
                        new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.LIFELINK), GrantScope.OWN_CREATURES)
                ),
                "{4}{W}{W}, Exile this card from your graveyard: Creatures you control get +2/+2 and gain first strike and lifelink until end of turn."
        ));
    }
}
