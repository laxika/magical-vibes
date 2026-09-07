package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "281")
public class KnowledgeVault extends Card {

    public KnowledgeVault() {
        addActivatedAbility(new ActivatedAbility(true, "{2}",
                List.of(new ExileTopCardsToSourceEffect(1, true)),
                "{2}, {T}: Exile the top card of your library face down."));

        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new SacrificeSelfThenEffect(SequenceEffect.of(
                        new DiscardHandEffect(),
                        new PutAllCardsExiledWithSourceIntoOwnersHandsEffect()))),
                "Sacrifice this artifact. If you do, discard your hand, then put all cards exiled with this artifact into their owner's hand."));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect());
    }
}
