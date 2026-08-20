package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.ValkmiraProtectorsShield;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "21")
public class ReidaneGodOfTheWorthy extends Card {

    public ReidaneGodOfTheWorthy() {
        setBackFaceCard(new ValkmiraProtectorsShield());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.STATIC, EnterPermanentsOfTypesTappedEffect.matching(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.SNOW))),
                true));
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        new CardMinManaValuePredicate(4))),
                2,
                CostModificationScope.OPPONENT));
    }

    @Override
    public String getBackFaceClassName() {
        return "ValkmiraProtectorsShield";
    }
}
