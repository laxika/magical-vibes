package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "78")
@CardRegistration(set = "ACR", collectorNumber = "126")
public class YggdrasilRebirthEngine extends Card {

    public YggdrasilRebirthEngine() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                ExileGraveyardCardsEffect.ownAllMatchingWithSource(
                        new CardTypePredicate(CardType.CREATURE)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileTopCardsToSourceEffect(3, false)),
                "{T}: Exile the top three cards of your library."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new ReturnCardExiledWithSourceToBattlefieldEffect(
                        new CardTypePredicate(CardType.CREATURE), false, null, true)),
                "{4}, {T}: Put a creature card exiled with Yggdrasil onto the battlefield under your control. "
                        + "It gains haste until end of turn. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
