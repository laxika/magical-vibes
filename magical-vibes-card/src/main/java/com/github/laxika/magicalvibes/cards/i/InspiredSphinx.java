package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GNT", collectorNumber = "2")
public class InspiredSphinx extends Card {

    public InspiredSphinx() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DrawCardEffect(new Sum(new PlayersInGame(), new Fixed(-1))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new CreateTokenEffect(
                        "Thopter", 1, 1, null,
                        List.of(CardSubtype.THOPTER),
                        Set.of(Keyword.FLYING),
                        Set.of(CardType.ARTIFACT)
                )),
                "{3}{U}: Create a 1/1 colorless Thopter artifact creature token with flying."
        ));
    }
}
