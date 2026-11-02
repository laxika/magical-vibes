package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.UnlockControlledRoomDoorEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "13")
public class GhostlyDancers extends Card {

    public GhostlyDancers() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ChooseOneAtResolutionEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return an enchantment card from your graveyard to your hand",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.ENCHANTMENT))
                                .build()),
                new ChooseOneEffect.ChooseOneOption(
                        "Unlock a locked door of a Room you control",
                        new UnlockControlledRoomDoorEffect())))));

        CreateTokenEffect spiritToken = new CreateTokenEffect(
                "Spirit", 3, 1, CardColor.WHITE,
                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of());
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), spiritToken);
        addTrigger(Triggers.youFullyUnlockRoom(), spiritToken);
    }
}
