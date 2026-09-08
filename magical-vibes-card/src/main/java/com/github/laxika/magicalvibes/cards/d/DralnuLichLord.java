package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DralnuDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "237")
public class DralnuLichLord extends Card {

    public DralnuLichLord() {
        addEffect(EffectSlot.STATIC, new DralnuDamageReplacementEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantFlashbackToTargetGraveyardCardEffect(Set.of(CardType.INSTANT, CardType.SORCERY))),
                "{T}: Target instant or sorcery card in your graveyard gains flashback until end of turn."
        ));
    }
}
