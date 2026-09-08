package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardCreateTokenIfLandOrMayCastUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "198")
public class BruseTarlRovingRancher extends Card {

    public BruseTarlRovingRancher() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DOUBLE_STRIKE,
                GrantScope.ALL_OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.OX)));

        var topCardEffect = new ExileTopCardCreateTokenIfLandOrMayCastUntilNextTurnEffect(
                new CreateTokenEffect("Ox", 2, 2, CardColor.WHITE, List.of(CardSubtype.OX),
                        Set.of(), Set.of()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, topCardEffect);
        addEffect(EffectSlot.ON_ATTACK, topCardEffect);
    }
}
