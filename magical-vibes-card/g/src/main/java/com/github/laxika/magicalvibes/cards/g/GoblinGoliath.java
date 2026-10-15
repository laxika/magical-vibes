package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyControllerDamageToOpponentsThisTurnEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GNT", collectorNumber = "4")
public class GoblinGoliath extends Card {

    public GoblinGoliath() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(new Sum(new PlayersInGame(), new Fixed(-1)),
                        "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{R}",
                List.of(new MultiplyControllerDamageToOpponentsThisTurnEffect(2)),
                "{3}{R}, {T}: If a source you control would deal damage to an opponent this turn, it deals double that damage to that player instead."
        ));
    }
}
