package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.PutTopCardsOfDamagedPlayerLibraryFaceDownAsCybermenEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "177")
@CardRegistration(set = "WHO", collectorNumber = "458")
@CardRegistration(set = "WHO", collectorNumber = "782")
@CardRegistration(set = "WHO", collectorNumber = "1049")
public class Cybership extends Card {

    public Cybership() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PutTopCardsOfDamagedPlayerLibraryFaceDownAsCybermenEffect(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(4), AnimatePermanentsEffect.crew()),
                "Crew 4"
        ));
    }
}
