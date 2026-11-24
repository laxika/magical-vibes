package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOneFromTopCardsFaceDownWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "BRC", collectorNumber = "15")
@CardRegistration(set = "BRC", collectorNumber = "62")
public class KaylasMusicBox extends Card {

    public KaylasMusicBox() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new ExileOneFromTopCardsFaceDownWithSourceEffect(1)),
                "{W}, {T}: Look at the top card of your library, then exile it face down."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(AllowPlayCardsFromExileEffect.thisTurn(Subjects.exiledWithSource(Subjects.you()))
                        .alsoCardsExiledLaterThisTurn()),
                "{T}: Until end of turn, you may play cards you own exiled with Kayla's Music Box."
        ));
    }
}
