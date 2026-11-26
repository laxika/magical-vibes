package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DamageFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardsCantEnterBattlefieldFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPlayLandsIfPermanentCountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.EnumSet;

@CardRegistration(set = "DRK", collectorNumber = "56")
public class WormsOfTheEarth extends Card {

    public WormsOfTheEarth() {
        addStaticEffect(new PlayersCantPlayLandsIfPermanentCountEffect(
                0, new PermanentTruePredicate()));
        addStaticEffect(new CardsCantEnterBattlefieldFromZonesEffect(
                new CardTypePredicate(CardType.LAND),
                EnumSet.complementOf(EnumSet.of(Zone.BATTLEFIELD))));
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), UnlessPaysEffect.mayPay(Subjects.eachPlayer(),
                AnyOfCost.of(new SacrificeMultiplePermanentsCost(2, new PermanentIsLandPredicate(), "lands"),
                        DamageFromSourceCost.of(5)),
                new DestroyTargetPermanentEffect(Subjects.source())));
    }
}
