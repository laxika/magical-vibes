package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DreadOsseosaur;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "129")
public class VisageOfDread extends Card {

    public VisageOfDread() {
        setBackFaceCard(new DreadOsseosaur());

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardsFromTargetHandEffect(
                1,
                List.of(),
                List.of(CardType.ARTIFACT, CardType.CREATURE),
                HandChoiceDestination.DISCARD
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{B}",
                List.of(
                        new ExileSelfCost(),
                        new CraftMaterialCost(2, CardType.CREATURE, false, false),
                        new ReturnSourceFromExileTransformedEffect()
                ),
                "{5}{B}, Exile this artifact, Exile the two from among creatures you control and/or creature cards in your graveyard: "
                        + "Return this card transformed under its owner's control. Craft only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "DreadOsseosaur";
    }
}
