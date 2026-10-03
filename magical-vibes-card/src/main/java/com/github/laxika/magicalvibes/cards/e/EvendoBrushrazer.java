package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedNontokenPermanentThisTurn;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "EOC", collectorNumber = "10")
@CardRegistration(set = "EOC", collectorNumber = "30")
public class EvendoBrushrazer extends Card {

    public EvendoBrushrazer() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        new ExileTopCardsToSourceEffect(1, false)));

        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(
                        new ControllerSacrificedNontokenPermanentThisTurn(),
                        new AllowCastFromCardsExiledWithSourceEffect(false, null, false, true, 0)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsLandPredicate(), "Sacrifice a land", false),
                        new AwardManaEffect(ManaColor.RED, 2)),
                "{T}, Sacrifice a land: Add {R}{R}."));
    }
}
