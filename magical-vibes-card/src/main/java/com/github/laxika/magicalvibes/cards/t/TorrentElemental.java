package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "56")
public class TorrentElemental extends Card {

    public TorrentElemental() {
        addEffect(EffectSlot.ON_ATTACK, new TapPermanentsEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentControlledByDefendingPlayerPredicate()));
        addExileActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B/G}{B/G}",
                List.of(new ReturnSourceCardFromExileToBattlefieldEffect(true)),
                "{3}{B/G}{B/G}: Put this card from exile onto the battlefield tapped. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
