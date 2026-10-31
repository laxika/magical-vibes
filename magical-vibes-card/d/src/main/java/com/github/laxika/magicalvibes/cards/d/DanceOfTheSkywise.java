package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "50")
public class DanceOfTheSkywise extends Card {

    public DanceOfTheSkywise() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN))
                .addSpellEffect(new GrantColorUntilEndOfTurnEffect(CardColor.BLUE))
                .addSpellEffect(new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(
                                List.of(CardSubtype.DRAGON, CardSubtype.ILLUSION)))
                .addSpellEffect(new SetBasePowerToughnessEffect(4, 4))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
    }
}
