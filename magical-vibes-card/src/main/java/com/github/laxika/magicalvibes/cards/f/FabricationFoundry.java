package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileArtifactsWithTotalManaValueCost;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "12")
public class FabricationFoundry extends Card {

    public FabricationFoundry() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(ManaColor.WHITE, 1, new ManaRestriction.ArtifactSpells())),
                "{T}: Add {W}. Spend this mana only to cast an artifact spell or activate an ability of an artifact source."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(
                        new ExileArtifactsWithTotalManaValueCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.ARTIFACT))
                                .targetGraveyard(true)
                                .requiresManaValueAtMostX(true)
                                .build()
                ),
                "{2}{W}, {T}, Exile one or more other artifacts you control with total mana value X: Return target artifact card with mana value X or less from your graveyard to the battlefield.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
