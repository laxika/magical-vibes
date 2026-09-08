package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetCreatureHexproofFromChosenColorAndCantBeBlockedByItUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "37")
public class SungoldSentinel extends Card {

    public SungoldSentinel() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD));
        addEffect(EffectSlot.ON_ATTACK,
                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new GrantTargetCreatureHexproofFromChosenColorAndCantBeBlockedByItUntilEndOfTurnEffect(true)),
                "Coven — {1}{W}: Choose a color. This creature gains hexproof from that color until end of turn "
                        + "and can't be blocked by creatures of that color this turn. Activate only if you control "
                        + "three or more creatures with different powers.",
                ActivationTimingRestriction.COVEN));
    }
}
