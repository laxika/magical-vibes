package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "51")
@CardRegistration(set = "FDN", collectorNumber = "314")
@CardRegistration(set = "FDN", collectorNumber = "380")
@CardRegistration(set = "FDN", collectorNumber = "457")
public class SphinxOfForgottenLore extends Card {

    public SphinxOfForgottenLore() {
        addTrigger(Triggers.attacks(), new GrantFlashbackToTargetGraveyardCardEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
    }
}
