package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "113")
public class CompanyCommander extends Card {

    public CompanyCommander() {
        // Command Section — When this creature enters, create a number of 1/1 white Soldier
        // creature tokens equal to the number of opponents you have.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                new Sum(new PlayersInGame(), new Fixed(-1)),
                "Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.SOLDIER), Set.of(), Set.of()));

        // Bring it Down! — Whenever this creature attacks, creatures you control gain deathtouch
        // until end of turn.
        addEffect(EffectSlot.ON_ATTACK,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_OWN_CREATURES));
    }
}
