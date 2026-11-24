package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "115")
public class DeathMaskDuplicant extends Card {

    public DeathMaskDuplicant() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        ExileGraveyardCardsEffect.oneTargetFromControllerGraveyardWithSource(
                                        new CardTypePredicate(CardType.CREATURE))
                                .imprintingOnSource()),
                "{1}: Exile target creature card from your graveyard."
        ));
        addStaticEffect(new GainKeywordsOfCreatureCardsExiledWithSourceEffect(
                Subjects.exiledWithSourceMatching(new CardTypePredicate(CardType.CREATURE)),
                Set.of(Keyword.FLYING, Keyword.FEAR, Keyword.FIRST_STRIKE, Keyword.DOUBLE_STRIKE, Keyword.HASTE,
                        Keyword.FORESTWALK, Keyword.MOUNTAINWALK, Keyword.ISLANDWALK, Keyword.SWAMPWALK,
                        Keyword.PLAINSWALK, Keyword.DESERTWALK, Keyword.TRAMPLE),
                true));
    }
}
