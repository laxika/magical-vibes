package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "KLD", collectorNumber = "32")
public class ToolcraftExemplar extends Card {

    public ToolcraftExemplar() {
        // At the beginning of combat on your turn, if you control an artifact, this creature
        // gets +2/+1 until end of turn. If you control three or more artifacts, it also gains
        // first strike until end of turn.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentIsArtifactPredicate()),
                SequenceEffect.of(
                        new BoostSelfEffect(2, 1),
                        new ConditionalEffect(new Metalcraft(),
                                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF))
                )
        ));
    }
}
