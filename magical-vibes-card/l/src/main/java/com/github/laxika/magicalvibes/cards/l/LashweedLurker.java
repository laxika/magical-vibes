package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "9")
public class LashweedLurker extends Card {

    public LashweedLurker() {
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{5}{G}{U}"),
                new SacrificePermanentsCost(1, new PermanentIsCreaturePredicate())
        ), true));

        target(TargetFilters.nonlandPermanent())
                .addTrigger(Triggers.youCastThisSpell(),
                        new MayEffect(new PutTargetOnTopOfLibraryEffect(),
                                "Put target nonland permanent on top of its owner's library?"));
    }
}
