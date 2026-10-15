package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "74")
public class VoidMaw extends Card {

    public VoidMaw() {
        addEffect(EffectSlot.STATIC, new ExilePermanentsInsteadOfGraveyardEffect(
                new PermanentIsCreaturePredicate(), true, true));
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PutCardExiledWithSourceIntoGraveyardCost(), new BoostSelfEffect(2, 2)),
                "Put a card exiled with Void Maw into its owner's graveyard: Void Maw gets +2/+2 until end of turn."));
    }
}
