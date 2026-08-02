package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "228")
public class CodexShredder extends Card {

    public CodexShredder() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillEffect(1, MillRecipient.TARGET_PLAYER)),
                "{T}: Target player mills a card."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new SacrificeSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .targetGraveyard(true)
                                .build()
                ),
                "{5}, {T}, Sacrifice this artifact: Return target card from your graveyard to your hand."
        ));
    }
}
