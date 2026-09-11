package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.UnlockControlledRoomDoorEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "13")
public class GhostlyDancers extends Card {

    public GhostlyDancers() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return an enchantment card from your graveyard to your hand",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.ENCHANTMENT))
                                .build()),
                new ChooseOneEffect.ChooseOneOption(
                        "Unlock a locked door of a Room you control",
                        new UnlockControlledRoomDoorEffect()))));

        CreateTokenEffect spiritToken = new CreateTokenEffect(
                "Spirit", 3, 1, CardColor.WHITE,
                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of());
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, spiritToken);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, spiritToken);
    }
}
