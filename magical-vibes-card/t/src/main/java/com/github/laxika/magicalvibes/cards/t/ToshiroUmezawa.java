package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GrantTargetGraveyardCardCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "89")
@CardRegistration(set = "SLD", collectorNumber = "261")
public class ToshiroUmezawa extends Card {

    public ToshiroUmezawa() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BushidoEffect(1));
        addTrigger(Triggers.becomesBlocked(), new BushidoEffect(1));
        // Whenever a creature an opponent controls dies, you may cast target instant card from your
        // graveyard. If that spell would be put into a graveyard, exile it instead.
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                new MayEffect(new GrantTargetGraveyardCardCastEffect(
                new CardTypePredicate(CardType.INSTANT),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                true), "Cast the target instant card from your graveyard?"));
    }
}
