package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfAttackingPlayerLibraryAndGrantControllerPlayPermissionEffect;

@CardRegistration(set = "C21", collectorNumber = "38")
public class CunningRhetoric extends Card {

    public CunningRhetoric() {
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(new OpponentAttacksWithAtLeastCreatures(1),
                        new ExileTopCardOfAttackingPlayerLibraryAndGrantControllerPlayPermissionEffect()));
    }
}
