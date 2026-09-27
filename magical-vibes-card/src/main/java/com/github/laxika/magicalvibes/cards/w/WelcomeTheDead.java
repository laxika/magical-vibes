package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsPutIntoGraveyardFromHandOrLibraryThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "30")
@CardRegistration(set = "TDC", collectorNumber = "70")
public class WelcomeTheDead extends Card {

    public WelcomeTheDead() {
        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new DrawCardEffect(2),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                new LoseLifeEffect(2),
                tappedBlackZombie(new CardsPutIntoGraveyardFromHandOrLibraryThisTurn())
        ));
        addCastingOption(new FlashbackCast("{5}{B}"));
    }

    private static CreateTokenEffect tappedBlackZombie(
            CardsPutIntoGraveyardFromHandOrLibraryThisTurn amount) {
        return new CreateTokenEffect(
                CardType.CREATURE, amount, "Zombie Druid", 2, 2, CardColor.BLACK, null,
                List.of(CardSubtype.ZOMBIE, CardSubtype.DRUID), Set.<Keyword>of(), Set.<CardType>of(), false, true,
                Map.of(), List.of(), false, false, false, 0, Set.of());
    }
}
