package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.condition.SourceEntryCostPaid;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.LandCasualtyEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "378")
@CardRegistration(set = "MB2", collectorNumber = "615")
public class MaestrosTotallySafeHideout extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("MaestrosTotallySafeHideout", new OracleData(
                "Maestros' Totally Safe Hideout",
                CardType.LAND,
                Set.of(),
                null,
                null,
                List.of(),
                List.of(CardColor.BLUE, CardColor.BLACK, CardColor.RED),
                Set.of(),
                List.of(),
                "Land casualty 2 (As this land enters, you may sacrifice a creature with power 2 or greater. "
                        + "If you do, create a token that's a copy of this land.)\n"
                        + "Maestros' Totally Safe Hideout enters the battlefield tapped.\n"
                        + "{T}: Add {U}, {B}, or {R}.",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public MaestrosTotallySafeHideout() {
        addEffect(EffectSlot.STATIC, new LandCasualtyEffect(2));
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new SourceEntryCostPaid(), new CreateTokenCopyOfSourceEffect()));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(
                        ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED))),
                "{T}: Add {U}, {B}, or {R}."));
    }
}
