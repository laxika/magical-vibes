package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "268")
public class SenseisDiviningTop extends Card {

    public SenseisDiviningTop() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ReorderTopCardsOfLibraryEffect(3)),
                "{1}: Look at the top three cards of your library, then put them back in any order."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(1), PutTargetOnTopOfLibraryEffect.self()),
                "{T}: Draw a card, then put this artifact on top of its owner's library."
        ));
    }
}
