package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "66")
public class CallousBloodmage extends Card {

    public CallousBloodmage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 black and green Pest creature token with \"When this token dies, you gain 1 life.\"",
                        pestToken()),
                new ChooseOneEffect.ChooseOneOption(
                        "You draw a card and you lose 1 life",
                        List.of(new DrawCardEffect(1), new LoseLifeEffect(1))),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target player's graveyard",
                        new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE))
        )));
    }

    private static CreateTokenEffect pestToken() {
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Pest", 1, 1,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.PEST), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_DEATH, new GainLifeEffect(1)),
                List.of(), false, false, false, 0, Set.of());
    }
}
