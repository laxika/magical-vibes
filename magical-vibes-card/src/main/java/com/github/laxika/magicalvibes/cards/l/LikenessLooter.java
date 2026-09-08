package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureCardInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "208")
public class LikenessLooter extends Card {

    public LikenessLooter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{T}: Draw a card, then discard a card."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(new BecomeCopyOfTargetCreatureCardInGraveyardEffect(
                        false, false, Set.of(Keyword.FLYING), 1)),
                "{X}: This creature becomes a copy of target creature card in your graveyard with mana value X, "
                        + "except it has flying and this ability. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
