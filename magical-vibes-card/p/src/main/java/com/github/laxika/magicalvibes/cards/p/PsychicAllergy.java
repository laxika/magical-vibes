package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "34")
public class PsychicAllergy extends Card {

    public PsychicAllergy() {
        addStaticEffect(new ChooseColorOnEnterEffect());

        var nontokenPermanentOfChosenColor = new PermanentAllOfPredicate(List.of(
                new PermanentHasSourceChosenColorPredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                new DealDamageToPlayersEffect(
                        new PermanentCount(nontokenPermanentOfChosenColor, CountScope.TARGET_PLAYER),
                        Subjects.targetPlayer()));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU), UnlessPaysEffect.unlessYouPay(new SacrificeMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.ISLAND), "Islands"), new DestroyTargetPermanentEffect(Subjects.source())));
    }
}
