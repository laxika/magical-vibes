package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

public class Flameshape extends Card {

    public Flameshape() {
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(2, false)
                .exiledFaceDown()
                .until(Duration.PERMANENT)
                .playableOnlyIf(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.WIZARD))));
    }
}
