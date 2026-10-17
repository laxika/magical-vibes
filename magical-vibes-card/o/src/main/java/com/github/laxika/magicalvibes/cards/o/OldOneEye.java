package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "96")
public class OldOneEye extends Card {

    public OldOneEye() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Tyranid", 5, 5, CardColor.GREEN, List.of(CardSubtype.TYRANID), Set.of(), Set.of()));
        // "You may discard two cards. If you do, return this card": both discards and the return
        // resolve within the one resolution, and the prompt is skipped with fewer than two cards.
        addEffect(EffectSlot.GRAVEYARD_PRECOMBAT_MAIN_TRIGGERED, new MayEffect(
                new ConditionalEffect(new CardsInHandAtLeast(2),
                        new DiscardCardThenEffect(null, new DiscardCardThenEffect(
                                null, new ReturnSourceCardFromGraveyardToOwnerHandEffect(), "a card",
                                null, false, null, null, DiscardRecipient.CONTROLLER, true), "a card",
                                null, false, null, null, DiscardRecipient.CONTROLLER, true)),
                "Discard two cards to return Old One Eye to your hand?"));
    }
}
