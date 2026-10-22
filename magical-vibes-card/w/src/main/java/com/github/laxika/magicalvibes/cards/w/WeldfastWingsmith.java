package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceMatchesPermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "KLD", collectorNumber = "69")
@CardRegistration(set = "KLR", collectorNumber = "72")
public class WeldfastWingsmith extends Card {

    public WeldfastWingsmith() {
        // Whenever an artifact you control enters, this creature gains flying until end of turn.
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
        // This creature is itself an artifact you control as it enters if an effect made it one (CR 603.6b),
        // so the same ability triggers for its own entry.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new SourceMatchesPermanentPredicate(new PermanentIsArtifactPredicate()),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
