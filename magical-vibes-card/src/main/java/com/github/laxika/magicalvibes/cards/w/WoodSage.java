package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureNameRevealTopCardsToHandRestToGraveyardEffect;
import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "275")
@CardRegistration(set = "TPR", collectorNumber = "216")
public class WoodSage extends Card {

    public WoodSage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ChooseCreatureNameRevealTopCardsToHandRestToGraveyardEffect(4)),
                "{T}: Choose a creature card name. Reveal the top four cards of your library and put "
                        + "all of them with that name into your hand. Put the rest into your graveyard."));
    }
}
