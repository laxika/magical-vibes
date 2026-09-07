package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;

import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "207")
public class JohannApprenticeSorcerer extends Card {

    public JohannApprenticeSorcerer() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, AllowCastFromTopOfLibraryEffect.onceEachTurn(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
    }
}
