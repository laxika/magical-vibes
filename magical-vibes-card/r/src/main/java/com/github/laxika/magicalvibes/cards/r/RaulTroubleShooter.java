package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastMilledSpellFromGraveyardOncePerYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "115")
@CardRegistration(set = "PIP", collectorNumber = "643")
public class RaulTroubleShooter extends Card {

    public RaulTroubleShooter() {
        addEffect(EffectSlot.STATIC,
                new CastMilledSpellFromGraveyardOncePerYourTurnEffect(new CardTruePredicate()));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new MillEffect(1, MillRecipient.CONTROLLER),
                        new MillEffect(1, MillRecipient.EACH_OPPONENT)),
                "{T}: Each player mills a card."));
    }
}
