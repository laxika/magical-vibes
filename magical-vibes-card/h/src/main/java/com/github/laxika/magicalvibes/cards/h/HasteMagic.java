package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "140")
public class HasteMagic extends Card {

    public HasteMagic() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(3, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.target()))
                .addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(1, false)
                        .until(Duration.UNTIL_YOUR_NEXT_END_STEP));
    }
}
