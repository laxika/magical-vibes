package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;

@CardRegistration(set = "C13", collectorNumber = "36")
@CardRegistration(set = "CMA", collectorNumber = "35")
public class CurseOfInertia extends Card {

    public CurseOfInertia() {
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS, new ConditionalEffect(new AttacksEnchantedPlayer(), new MayEffect(
                new TapOrUntapTargetPermanentEffect(),
                "Tap or untap target permanent?",
                null,
                MayChoicePlayer.ACTIVE_PLAYER)));
    }
}
