package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GravestormEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKC", collectorNumber = "23")
@CardRegistration(set = "MKC", collectorNumber = "333")
public class FollowTheBodies extends Card {

    public FollowTheBodies() {
        addSpellEffect(CreateTokenEffect.ofClueToken(1));
        addTrigger(Triggers.youCastThisSpell(), new GravestormEffect());
    }
}
