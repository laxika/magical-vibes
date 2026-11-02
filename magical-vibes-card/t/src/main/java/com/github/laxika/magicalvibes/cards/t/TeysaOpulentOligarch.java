package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.OpponentsWhoLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "234")
@CardRegistration(set = "MKM", collectorNumber = "321")
@CardRegistration(set = "MKM", collectorNumber = "370")
public class TeysaOpulentOligarch extends Card {

    public TeysaOpulentOligarch() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                CreateTokenEffect.ofClueToken(new OpponentsWhoLostLifeThisTurn()));

        CreateTokenEffect spirit = new CreateTokenEffect(
                1, "Spirit", 1, 1, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.SPIRIT),
                Set.of(Keyword.FLYING), Set.of());
        addTrigger(Triggers.dies(new PermanentHasSubtypePredicate(CardSubtype.CLUE), ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                spirit);
    }
}
