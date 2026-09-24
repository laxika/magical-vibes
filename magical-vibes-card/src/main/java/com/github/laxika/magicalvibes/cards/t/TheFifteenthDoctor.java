package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1584")
public class TheFifteenthDoctor extends Card {

    public TheFifteenthDoctor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, millAndReturnArtifact());
        addEffect(EffectSlot.ON_ATTACK, millAndReturnArtifact());
        addEffect(EffectSlot.STATIC, new GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect(
                Keyword.IMPROVISE, new CardNotPredicate(new CardTypePredicate(CardType.ARTIFACT))));
    }

    private static MillControllerAndMayReturnMilledPermanentToHandEffect millAndReturnArtifact() {
        return new MillControllerAndMayReturnMilledPermanentToHandEffect(3, new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardMinManaValuePredicate(2),
                new CardMaxManaValuePredicate(3))));
    }
}
