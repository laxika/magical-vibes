package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BoneOffering;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "139")
public class AltarOfBhaal extends Card {

    public AltarOfBhaal() {
        setBackFaceCard(new BoneOffering());
        addCastingOption(new AdventureCast("{2}{B}"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(
                        new ExilePermanentCost(new PermanentIsCreaturePredicate(), "a creature", false),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .build()
                ),
                "{2}{B}, {T}, Exile a creature you control: Return target creature card from your graveyard to the battlefield.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "BoneOffering";
    }
}
