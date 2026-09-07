package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "71")
public class VantressGargoyle extends Card {

    public VantressGargoyle() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new OpponentGraveyardAtLeast(7),
                "defending player has seven or more cards in their graveyard"));
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new CardsInHandAtLeast(4),
                "you have four or more cards in hand"));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new MillEffect(1, MillRecipient.CONTROLLER),
                        new MillEffect(1, MillRecipient.EACH_OPPONENT)),
                "{T}: Each player mills a card."));
    }
}
