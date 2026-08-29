package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.KayaSpiritsJusticeEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "211")
@CardRegistration(set = "MKM", collectorNumber = "335")
public class KayaSpiritsJustice extends Card {

    public KayaSpiritsJustice() {
        addEffect(EffectSlot.ON_CONTROLLER_CREATURES_OR_CREATURE_CARDS_EXILED,
                new KayaSpiritsJusticeEffect());

        addActivatedAbility(new ActivatedAbility(
                2,
                List.of(new SurveilEffect(2), new ExileUpToOneCardFromGraveyardEffect(true)),
                "+2: Surveil 2, then exile a card from a graveyard."));

        addActivatedAbility(new ActivatedAbility(
                1,
                List.of(new CreateTokenEffect(1, "Spirit", 1, 1, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.SPIRIT),
                        Set.of(Keyword.FLYING), Set.of())),
                "+1: Create a 1/1 white and black Spirit creature token with flying."));

        ActivatedAbility exileCreatures = new ActivatedAbility(
                false,
                null,
                List.of(new ExileTargetPermanentEffect()),
                "-2: Exile target creature you control. For each other player, exile up to one target creature that player controls.",
                TargetFilters.creatureAnOpponentControls(),
                -2,
                null,
                null,
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls()),
                1,
                99
        ).withMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        addActivatedAbility(exileCreatures);
    }
}
