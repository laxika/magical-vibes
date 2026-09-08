package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantSearchLibrariesEffect;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "228")
public class AshiokDreamRender extends Card {

    public AshiokDreamRender() {
        addEffect(EffectSlot.STATIC, new OpponentsCantSearchLibrariesEffect());

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new MillEffect(4, MillRecipient.TARGET_PLAYER),
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_OPPONENTS)),
                "−1: Target player mills four cards. Then exile each opponent's graveyard."
        ));
    }
}
