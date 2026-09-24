package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2103")
@CardRegistration(set = "MH1", collectorNumber = "128")
@CardRegistration(set = "TSR", collectorNumber = "345")
public class GoblinEngineer extends Card {

    public GoblinEngineer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(
                        new Fixed(1),
                        new CardTypePredicate(CardType.ARTIFACT),
                        LibrarySearchDestination.GRAVEYARD),
                "Search your library for an artifact card?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.ARTIFACT),
                                        new CardMaxManaValuePredicate(3))))
                                .targetGraveyard(true)
                                .build()),
                "{R}, {T}, Sacrifice an artifact: Return target artifact card with mana value 3 or less "
                        + "from your graveyard to the battlefield."
        ));
    }
}
