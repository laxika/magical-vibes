package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "93")
@CardRegistration(set = "FIC", collectorNumber = "183")
public class SetzerWanderingGambler extends Card {

    public SetzerWanderingGambler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, blackjackToken());
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE),
                        new FlipCoinWinEffect(null)));
        addEffect(EffectSlot.ON_CONTROLLER_WINS_COIN_FLIP, CreateTokenEffect.ofTappedTreasureToken(2));
    }

    private static CreateTokenEffect blackjackToken() {
        return new CreateTokenEffect(
                CardType.ARTIFACT,
                1,
                "The Blackjack",
                3,
                3,
                null,
                null,
                List.of(CardSubtype.VEHICLE),
                Set.of(Keyword.FLYING),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(new ActivatedAbility(
                        false,
                        null,
                        List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                        "Crew 2"
                )),
                false,
                false,
                true,
                0,
                Set.of()
        );
    }
}
