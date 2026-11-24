package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatEffect;

@CardRegistration(set = "C19", collectorNumber = "32")
public class FullFlowering extends Card {

    public FullFlowering() {
        // "Populate X times": each populate is its own choice and may copy a token an earlier one made.
        addSpellEffect(new RepeatEffect(new XValue(), new PopulateEffect()));
    }
}
