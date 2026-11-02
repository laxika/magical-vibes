package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
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
                        DamageRecipient.TARGET_PLAYER));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ForcedCostOrElseEffect(
                new SacrificeMultiplePermanentsCost(2,
                        new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                List.of(new DestroyReferencedPermanentEffect(PermanentReference.SOURCE)),
                true));
    }
}
