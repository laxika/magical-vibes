package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RescueTheFoal;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "97")
public class PegasusGuardian extends Card {

    public PegasusGuardian() {
        setBackFaceCard(new RescueTheFoal());
        addCastingOption(new AdventureCast("{1}{W}"));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new PermanentLeftBattlefieldUnderYourControlThisTurn()),
                new CreateTokenEffect("Pegasus", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.PEGASUS), Set.of(Keyword.FLYING), Set.of()));
    }

    @Override
    public String getBackFaceClassName() {
        return "RescueTheFoal";
    }
}
