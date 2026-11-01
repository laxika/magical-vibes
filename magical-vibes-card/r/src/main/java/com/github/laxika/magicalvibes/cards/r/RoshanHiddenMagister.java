package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToOwnCreaturesInAllZonesEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ACR", collectorNumber = "32")
@CardRegistration(set = "ACR", collectorNumber = "133")
public class RoshanHiddenMagister extends Card {

    public RoshanHiddenMagister() {
        addStaticEffect(new GrantSubtypeToOwnCreaturesInAllZonesEffect(CardSubtype.ASSASSIN));
        addStaticEffect(new GrantKeywordEffect(
                Keyword.MENACE, GrantScope.OWN_CREATURES, new PermanentIsFaceDownPredicate()));
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.YOU_CONTROL),
                SequenceEffect.of(new DrawCardEffect(), new LoseLifeEffect(1)));
    }
}
