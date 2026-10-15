package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "196")
public class WhiteTigerAvaAyala extends Card {

    public WhiteTigerAvaAyala() {
        CreateTokenEffect tigerGod = new CreateTokenEffect(
                CardType.CREATURE, 1, "The Tiger God", 4, 4,
                CardColor.GREEN, null, List.of(CardSubtype.CAT, CardSubtype.GOD), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.STATIC, new CanBeBlockedByAtMostNCreaturesEffect(1)), List.of(),
                false, false, true, 0, Set.of());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        tigerGod
                ),
                "Power-up — {5}{G}: Put a +1/+1 counter on White Tiger and create The Tiger God, a legendary 4/4 green Cat God creature token with \"The Tiger God can't be blocked by more than one creature.\" Activate each power-up ability only once. Reduce the cost by her mana cost if she entered this turn."
        ).withPowerUp());
    }
}
