package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameForDelayedCreatureCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextSpellCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "28")
@CardRegistration(set = "SPM", collectorNumber = "219")
public class TheCloneSaga extends Card {

    public TheCloneSaga() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new SurveilEffect(3));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new CopyNextSpellCastThisTurnEffect(
                new CardTypePredicate(CardType.CREATURE), Set.of(CardSupertype.LEGENDARY)));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ChooseCardNameForDelayedCreatureCombatDamageEffect(
                List.of(new DrawCardEffect())));
    }
}
