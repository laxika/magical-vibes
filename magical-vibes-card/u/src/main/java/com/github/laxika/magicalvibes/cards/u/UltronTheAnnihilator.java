package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "668")
public class UltronTheAnnihilator extends Card {

    private static final CreateTokenEffect ROBOT_TOKEN = new CreateTokenEffect(
            1, "Robot", 2, 2, null,
            List.of(CardSubtype.ROBOT, CardSubtype.VILLAIN), Set.of(), Set.of(CardType.ARTIFACT));
    private static final LoseLifeEffect DRAIN = new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT);

    public UltronTheAnnihilator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ROBOT_TOKEN);
        addEffect(EffectSlot.ON_ATTACK, ROBOT_TOKEN);
        addEffect(EffectSlot.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardTypePredicate(CardType.ARTIFACT), DRAIN));
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD, DRAIN);
    }
}
