package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveOneOrMoreCountersFromSourceCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "40")
@CardRegistration(set = "DMC", collectorNumber = "62")
public class RasputinTheOneiromancer extends Card {

    public RasputinTheOneiromancer() {
        // Rasputin enters with one dream counter for each opponent, and each opponent creates a Goblin.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.DREAM, new Sum(new PlayersInGame(), new Fixed(-1))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachOpponentCreatesTokenEffect(goblinToken()));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveOneOrMoreCountersFromSourceCost(CounterType.DREAM),
                        new AwardManaEffect(ManaColor.COLORLESS, new XValue())
                ),
                "{T}, Remove one or more dream counters from Rasputin: Add that much {C}."
        ).withXValue());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.DREAM),
                        new CreateTokenEffect(1, "Knight", 2, 2, CardColor.WHITE,
                                List.of(CardSubtype.KNIGHT), Set.of(), Set.of(),
                                Map.of(EffectSlot.STATIC,
                                        new ProtectionFromColorsEffect(Set.of(CardColor.RED))))
                ),
                "{T}, Remove a dream counter from Rasputin: Create a 2/2 white Knight creature token with protection from red."
        ));
    }

    private static CreateTokenEffect goblinToken() {
        return new CreateTokenEffect("Goblin", 1, 1, CardColor.RED,
                List.of(CardSubtype.GOBLIN), Set.of(), Set.of());
    }
}
