package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureTokenLandToBattlefieldElseGainLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "207")
public class DruidicSatchel extends Card {

    public DruidicSatchel() {
        // {2}, {T}: Reveal the top card of your library. If it's a creature card, create a 1/1 green
        // Saproling creature token. If it's a land card, put that card onto the battlefield under your
        // control. If it's a noncreature, nonland card, you gain 2 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new RevealTopCardCreatureTokenLandToBattlefieldElseGainLifeEffect(
                        new CreateTokenEffect(1, "Saproling", 1, 1,
                                CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of()),
                        2)),
                "{2}, {T}: Reveal the top card of your library. If it's a creature card, create a 1/1 green "
                        + "Saproling creature token. If it's a land card, put that card onto the battlefield "
                        + "under your control. If it's a noncreature, nonland card, you gain 2 life."
        ));
    }
}
