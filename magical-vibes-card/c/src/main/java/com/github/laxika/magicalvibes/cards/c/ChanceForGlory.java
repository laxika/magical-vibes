package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterLoseGameAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "GRN", collectorNumber = "159")
@CardRegistration(set = "SLD", collectorNumber = "2470")
public class ChanceForGlory extends Card {

    public ChanceForGlory() {
        // "Creatures you control gain indestructible."
        addSpellEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.otherCreaturesYouControl(), Duration.PERMANENT));
        // "Take an extra turn after this one."
        addSpellEffect(new ControllerExtraTurnEffect(1));
        // "At the beginning of that turn's end step, you lose the game."
        addSpellEffect(new RegisterLoseGameAtEndStepEffect());
    }
}
