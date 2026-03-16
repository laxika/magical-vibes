package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.InsectileAberration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealTypeTransformEffect;

import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "51")
public class DelverOfSecrets extends Card {

    public DelverOfSecrets() {
        // Set up back face
        InsectileAberration backFace = new InsectileAberration();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // At the beginning of your upkeep, look at the top card of your library.
        // You may reveal that card. If an instant or sorcery card is revealed this way,
        // transform Delver of Secrets.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new LookAtTopCardMayRevealTypeTransformEffect(Set.of(CardType.INSTANT, CardType.SORCERY)));
    }

    @Override
    public String getBackFaceClassName() {
        return "InsectileAberration";
    }
}
