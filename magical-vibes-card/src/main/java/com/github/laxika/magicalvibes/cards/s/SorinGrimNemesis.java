package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.HighestLifeTotalAmongPlayers;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandThenEachOpponentLosesLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "251")
public class SorinGrimNemesis extends Card {

    public SorinGrimNemesis() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new RevealTopCardPutIntoHandThenEachOpponentLosesLifeEffect()),
                "+1: Reveal the top card of your library and put that card into your hand. Each opponent loses life equal to its mana value."
        ));

        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(new XValue()),
                        new GainLifeEffect(new XValue())
                ),
                "−X: Sorin, Grim Nemesis deals X damage to target creature or planeswalker and you gain X life.",
                null
        ));

        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(new CreateTokenEffect(
                        new HighestLifeTotalAmongPlayers(), "Vampire Knight", 1, 1,
                        CardColor.BLACK, List.of(CardSubtype.VAMPIRE, CardSubtype.KNIGHT),
                        Set.of(Keyword.LIFELINK), Set.of()
                )),
                "−9: Create a number of 1/1 black Vampire Knight creature tokens with lifelink equal to the highest life total among all players."
        ));
    }
}
