package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdditionalCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "212")
@CardRegistration(set = "SLD", collectorNumber = "1235")
@CardRegistration(set = "MUL", collectorNumber = "61")
@CardRegistration(set = "MUL", collectorNumber = "126")
@CardRegistration(set = "MUL", collectorNumber = "191")
@CardRegistration(set = "CMM", collectorNumber = "359")
@CardRegistration(set = "CMM", collectorNumber = "593")
@CardRegistration(set = "CMM", collectorNumber = "688")
public class TeysaKarlov extends Card {

    public TeysaKarlov() {
        addEffect(EffectSlot.STATIC,
                new AdditionalCreatureDeathTriggerEffect(new PermanentTruePredicate()));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.VIGILANCE, Keyword.LIFELINK), GrantScope.OWN_CREATURES,
                new PermanentIsTokenPredicate()));
    }
}
