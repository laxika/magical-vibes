package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardsCantBeTargetedEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "182")
public class SilentGravestone extends Card {

    public SilentGravestone() {
        addEffect(EffectSlot.STATIC, new GraveyardCardsCantBeTargetedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new ExileSelfEffect(),
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS),
                        new DrawCardEffect()
                ),
                "{4}, {T}: Exile this artifact and all cards from all graveyards. Draw a card."
        ));
    }
}
