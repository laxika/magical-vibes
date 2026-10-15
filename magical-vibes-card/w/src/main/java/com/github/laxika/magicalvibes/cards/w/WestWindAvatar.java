package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "137")
public class WestWindAvatar extends Card {

    public WestWindAvatar() {
        var tokenOrLand = new PermanentAnyOfPredicate(List.of(
                new PermanentIsTokenPredicate(),
                new PermanentIsLandPredicate()));
        var sacrificeForLife = new MayEffect(
                new SacrificePermanentThenEffect(tokenOrLand, new GainLifeEffect(3),
                        "a token or a land", false, false),
                "Sacrifice a token or a land?");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, sacrificeForLife);
        addEffect(EffectSlot.ON_ATTACK, sacrificeForLife);

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new PermanentLeftBattlefieldUnderYourControlThisTurn(),
                        new DrawCardEffect()));
    }
}
