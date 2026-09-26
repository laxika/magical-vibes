package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "16")
@CardRegistration(set = "LTC", collectorNumber = "100")
public class LossarnachCaptain extends Card {

    public LossarnachCaptain() {
        // Whenever this creature or another Human you control enters, tap target creature an opponent controls.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                        new TriggeringCardConditionalEffect(
                                new CardSubtypePredicate(CardSubtype.HUMAN),
                                new TapPermanentsEffect(TapUntapScope.TARGET)));

        // At the beginning of your upkeep, create a 1/1 white Human Soldier creature token.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenEffect(
                1,
                "Human Soldier",
                1,
                1,
                CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                Set.of(),
                Set.of()));
    }
}
