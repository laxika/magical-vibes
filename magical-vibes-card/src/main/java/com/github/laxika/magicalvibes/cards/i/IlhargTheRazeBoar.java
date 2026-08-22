package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromExileIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WAR", collectorNumber = "133")
public class IlhargTheRazeBoar extends Card {

    public IlhargTheRazeBoar() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                PutCardToBattlefieldEffect.tappedAndAttacking(new CardTypePredicate(CardType.CREATURE), "creature")
                        .returningToHandAtEndStep(),
                "Put a creature card from your hand onto the battlefield tapped and attacking?"));

        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2),
                "Put Ilharg, the Raze-Boar into its owner's library third from the top?"));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new SelfExiledFromBattlefieldEffect(
                new MayEffect(
                        new PutSourceCardFromExileIntoLibraryNFromTopEffect(2),
                        "Put Ilharg, the Raze-Boar into its owner's library third from the top?")));
    }
}
