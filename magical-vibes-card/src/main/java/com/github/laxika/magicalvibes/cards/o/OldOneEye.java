package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
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
        addEffect(EffectSlot.GRAVEYARD_PRECOMBAT_MAIN_TRIGGERED, new MayEffect(
                new DiscardCardThenEffect(null, new DiscardCardThenEffect(
                        null, new ReturnSourceCardFromGraveyardToOwnerHandEffect(), "a card"), "a card"),
                "Discard two cards to return Old One Eye to your hand?"));
    }
}
