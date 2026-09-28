package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TakeATripTo;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "384")
@CardRegistration(set = "MB2", collectorNumber = "621")
public class ValueTown extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("ValueTown", new OracleData(
                "Value Town",
                CardType.LAND,
                Set.of(),
                null,
                null,
                List.of(),
                List.of(CardColor.BLUE, CardColor.RED),
                Set.of(),
                List.of(CardSubtype.TOWN),
                "Value Town enters the battlefield tapped.\n"
                        + "{T}: Add {U} or {R}.",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public ValueTown() {
        setBackFaceCard(new TakeATripTo());
        addCastingOption(new AdventureCast("{4}{U}{R}"));
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.RED))),
                "{T}: Add {U} or {R}."));
    }

    @Override
    public String getBackFaceClassName() {
        return "TakeATripTo";
    }
}
