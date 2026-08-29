package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BRO", collectorNumber = "85")
public class AshnodsIntervention extends Card {

    public AshnodsIntervention() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 0))
                .addEffect(EffectSlot.SPELL, new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_DEATH, new ReturnSourceCardFromGraveyardToOwnerHandEffect()))
                .addEffect(EffectSlot.SPELL, new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                        new SelfExiledFromBattlefieldEffect(new ReturnSourceCardFromExileToOwnerHandEffect())));
    }
}
