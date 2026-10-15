package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "242")
public class VisionarysDance extends Card {

    public VisionarysDance() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Elemental", 3, 3,
                CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED),
                List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.FLYING), Set.of()));

        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(LookAtTopCardsEffect.chooseNToHandRestToGraveyard(2, 1)),
                "Discard this card: Look at the top two cards of your library. Put one of them into your hand and the other into your graveyard."));
    }
}
