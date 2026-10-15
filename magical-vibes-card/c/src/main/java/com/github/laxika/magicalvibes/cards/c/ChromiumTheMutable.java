package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesSubtypeUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "214")
public class ChromiumTheMutable extends Card {

    public ChromiumTheMutable() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.HUMAN),
                        new SetBasePowerToughnessEffect(1, 1, GrantScope.SELF),
                        new LosesAllAbilitiesEffect(GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN),
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF),
                        new MakeCreatureUnblockableEffect(true)
                ),
                "Discard a card: Until end of turn, Chromium becomes a Human with base power and toughness 1/1, loses all abilities, and gains hexproof. It can't be blocked this turn."
        ));
    }
}
