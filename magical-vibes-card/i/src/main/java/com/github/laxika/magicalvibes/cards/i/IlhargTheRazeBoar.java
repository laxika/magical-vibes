package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromExileIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "133")
@CardRegistration(set = "SLD", collectorNumber = "318")
@CardRegistration(set = "SLD", collectorNumber = "2402")
@CardRegistration(set = "RVR", collectorNumber = "113")
public class IlhargTheRazeBoar extends Card {

    public IlhargTheRazeBoar() {
        addTrigger(Triggers.attacks(), new MayEffect(
                PutCardToBattlefieldEffect.tappedAndAttacking(new CardTypePredicate(CardType.CREATURE), "creature")
                        .returningToHandAtEndStep(),
                "Put a creature card from your hand onto the battlefield tapped and attacking?"));

        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2),
                "Put Ilharg, the Raze-Boar into its owner's library third from the top?"));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new SelfExiledFromBattlefieldEffect(
                new MayEffect(
                        new PutSourceCardFromExileIntoLibraryNFromTopEffect(2),
                        "Put Ilharg, the Raze-Boar into its owner's library third from the top?")));
    }
}
