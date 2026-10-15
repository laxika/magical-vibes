package com.github.laxika.magicalvibes.cards.t;

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

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "102")
public class TyranidInvasion extends Card {

    public TyranidInvasion() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new Sum(new PlayersInGame(), new Fixed(-1)),
                "Tyranid Warrior", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.TYRANID, CardSubtype.WARRIOR), Set.of(Keyword.TRAMPLE), Set.of()));
    }
}
