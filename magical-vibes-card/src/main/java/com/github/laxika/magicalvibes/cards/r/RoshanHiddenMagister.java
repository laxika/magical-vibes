package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToOwnCreaturesInAllZonesEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

@CardRegistration(set = "ACR", collectorNumber = "32")
@CardRegistration(set = "ACR", collectorNumber = "133")
public class RoshanHiddenMagister extends Card {

    public RoshanHiddenMagister() {
        addEffect(EffectSlot.STATIC,
                new GrantSubtypeToOwnCreaturesInAllZonesEffect(CardSubtype.ASSASSIN));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.MENACE, GrantScope.OWN_CREATURES, new PermanentIsFaceDownPredicate()));
        addEffect(EffectSlot.ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP,
                SequenceEffect.of(new DrawCardEffect(), new LoseLifeEffect(1)));
    }
}
