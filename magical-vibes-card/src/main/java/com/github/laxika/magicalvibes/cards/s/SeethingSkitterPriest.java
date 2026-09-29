package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "YONE", collectorNumber = "27")
public class SeethingSkitterPriest extends Card {

    public SeethingSkitterPriest() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyGrantTriggeredAbilityToMatchingCardsEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        new PermanentIsCreaturePredicate(),
                        EffectSlot.ON_DEATH,
                        phyrexianMite()));
    }

    private static CreateTokenEffect phyrexianMite() {
        return new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Mite",
                1,
                1,
                null,
                null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.MITE),
                Set.of(Keyword.TOXIC),
                Set.of(CardType.ARTIFACT),
                false,
                false,
                Map.of(
                        EffectSlot.STATIC, new CantBlockEffect(),
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER)),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());
    }
}
