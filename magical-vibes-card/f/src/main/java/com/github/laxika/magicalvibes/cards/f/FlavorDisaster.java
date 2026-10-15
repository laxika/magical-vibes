package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "338")
@CardRegistration(set = "MB2", collectorNumber = "575")
public class FlavorDisaster extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("FlavorDisaster", new OracleData(
                "Flavor Disaster",
                CardType.CREATURE,
                Set.of(),
                "{4}{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(CardSubtype.ELEMENTAL, CardSubtype.PIRATE),
                "Reach\nWhen Flavor Disaster enters the battlefield or when it's turned face up, "
                        + "target creature gets +X/+X until end of turn, where X is Flavor Disaster's power.\n"
                        + "Negamorph {1}{G}",
                4,
                3,
                Set.of(Keyword.REACH),
                null,
                null,
                null));
    }

    public FlavorDisaster() {
        addMorph("{1}{G}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new PutCountersOnTurnFaceUpEffect(CounterType.MINUS_ONE_MINUS_ONE, 1, false));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new BoostTargetCreatureEffect(new SourcePower(), new SourcePower()))
                .addEffect(EffectSlot.ON_TURNED_FACE_UP,
                        new BoostTargetCreatureEffect(new SourcePower(), new SourcePower()));
    }
}
