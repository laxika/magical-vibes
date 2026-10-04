package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "ELD", collectorNumber = "321")
public class EmberethSkyblazer extends Card {

    public EmberethSkyblazer() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));

        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect("{2}{R}",
                new BoostAllOwnCreaturesEffect(
                        new Sum(new PlayersInGame(), new Fixed(-1)), new Fixed(0)),
                "Pay {2}{R} to boost creatures you control?"));
    }
}
