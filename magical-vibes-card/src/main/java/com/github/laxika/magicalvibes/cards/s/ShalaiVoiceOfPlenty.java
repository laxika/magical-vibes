package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantControllerHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "35")
public class ShalaiVoiceOfPlenty extends Card {

    public ShalaiVoiceOfPlenty() {
        // You have hexproof.
        addEffect(EffectSlot.STATIC, new GrantControllerHexproofEffect());

        // Planeswalkers you control have hexproof.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_PERMANENTS,
                new PermanentIsPlaneswalkerPredicate()));

        // Other creatures you control have hexproof.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_CREATURES));

        // {4}{G}{G}: Put a +1/+1 counter on each creature you control.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}{G}",
                List.of(new PutPlusOnePlusOneCounterOnEachOwnCreatureEffect()),
                "{4}{G}{G}: Put a +1/+1 counter on each creature you control."
        ));
    }
}
