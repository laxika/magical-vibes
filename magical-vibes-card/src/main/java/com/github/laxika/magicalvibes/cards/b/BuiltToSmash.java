package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KLD", collectorNumber = "108")
public class BuiltToSmash extends Card {

    public BuiltToSmash() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3))
                .addEffect(EffectSlot.SPELL, GrantKeywordEffect.toTargetIf(
                        Keyword.TRAMPLE, new PermanentIsArtifactPredicate()));
    }
}
