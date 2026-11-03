package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addTrigger(Triggers.enters(ObjectRelation.THIS), sacrificeForLife);
        addTrigger(Triggers.attacks(), sacrificeForLife);

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new PermanentLeftBattlefieldUnderYourControlThisTurn()),
                new DrawCardEffect());
    }
}
