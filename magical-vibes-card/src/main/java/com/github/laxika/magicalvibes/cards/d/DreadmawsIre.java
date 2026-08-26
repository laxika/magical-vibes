package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "147")
public class DreadmawsIre extends Card {

    public DreadmawsIre() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL, new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentControlledByDefendingPlayerPredicate())))));
    }
}
