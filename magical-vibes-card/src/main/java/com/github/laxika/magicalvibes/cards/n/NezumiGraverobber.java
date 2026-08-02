package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "129")
public class NezumiGraverobber extends Card {

    public NezumiGraverobber() {
        setBackFaceCard(new NighteyesTheDesecrator());

        // {1}{B}: Exile target card from an opponent's graveyard. If no cards are in that
        // graveyard, flip this creature. The exile resolves first, so the flip condition sees
        // the graveyard after the exile; in this two-player engine "that graveyard" is the only
        // opponent's, so "no opponent has a card in their graveyard" is the same check.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD),
                        new ConditionalEffect(new NotCondition(new OpponentGraveyardAtLeast(1)),
                                new TransformToBackFaceEffect())
                ),
                "{1}{B}: Exile target card from an opponent's graveyard. If no cards are in that graveyard, flip this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "NighteyesTheDesecrator";
    }
}
