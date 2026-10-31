package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "KLD", collectorNumber = "79")
@CardRegistration(set = "KLR", collectorNumber = "82")
public class EmbraalBruiser extends Card {

    public EmbraalBruiser() {
        addStaticEffect(new EntersTappedEffect());
        addStaticEffect(new ConditionalEffect(
                new ControlsPermanent(new PermanentIsArtifactPredicate()),
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)));
    }
}
