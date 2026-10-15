package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MIC", collectorNumber = "17")
@CardRegistration(set = "MIC", collectorNumber = "55")
@CardRegistration(set = "DRC", collectorNumber = "88")
public class CrowdedCrypt extends Card {

    public CrowdedCrypt() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new PutCountersOnSelfEffect(CounterType.CORPSE));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{B}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                new CountersOnSource(CounterType.CORPSE),
                                "Zombie",
                                2,
                                2,
                                CardColor.BLACK,
                                null,
                                List.of(CardSubtype.ZOMBIE),
                                Set.of(Keyword.DECAYED),
                                Set.of(),
                                false,
                                false,
                                Map.of(
                                        EffectSlot.STATIC, new CantBlockEffect(),
                                        EffectSlot.ON_ATTACK, new SacrificeAtEndOfCombatEffect()),
                                List.of(),
                                false,
                                false,
                                false,
                                0,
                                Set.of())),
                "{4}{B}{B}, {T}, Sacrifice this artifact: Create a 2/2 black Zombie creature token with decayed for each corpse counter on this artifact."));
    }
}
