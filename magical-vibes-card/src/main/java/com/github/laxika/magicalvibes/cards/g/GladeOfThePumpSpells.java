package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "339")
@CardRegistration(set = "MB2", collectorNumber = "576")
public class GladeOfThePumpSpells extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("GladeOfThePumpSpells", new OracleData(
                "Glade of the Pump Spells",
                CardType.LAND,
                Set.of(),
                "{2}{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(),
                "(You have to pay {2}{G} to play this land as your land drop. It's green.)\n"
                        + "When Glade of the Pump Spells enters the battlefield, up to one target creature "
                        + "gets +2/+2 and gains trample until end of turn.\n"
                        + "{T}: Add {G}{G}.",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public GladeOfThePumpSpells() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostTargetCreatureEffect(2, 2))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN, 2)),
                "{T}: Add {G}{G}."));
    }
}
