package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerToAmountUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "266")
public class PuPuUFO extends Card {

    public PuPuUFO() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land"),
                        "Put a land card from your hand onto the battlefield?"
                )),
                "{T}: You may put a land card from your hand onto the battlefield."
        ));

        PermanentCount towns = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.TOWN), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new SetSelfBasePowerToAmountUntilEndOfTurnEffect(towns)),
                "{3}: Until end of turn, this creature's base power becomes equal to the number of Towns you control."
        ));
    }
}
