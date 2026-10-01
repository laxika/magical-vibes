package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "108")
@CardRegistration(set = "WHO", collectorNumber = "713")
public class TheSeaDevils extends Card {

    private static final CreateTokenEffect SALAMANDER_TOKEN = new CreateTokenEffect(
        1, "Alien Salamander", 2, 2, CardColor.GREEN,
            List.of(CardSubtype.SALAMANDER), Set.of(Keyword.ISLANDWALK), Set.of());

    public TheSeaDevils() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, SALAMANDER_TOKEN);
        addEffect(EffectSlot.SAGA_CHAPTER_II, SALAMANDER_TOKEN);
        addEffect(EffectSlot.SAGA_CHAPTER_III, new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DealDamageToTargetCreatureDamagedPlayerControlsEffect(new EventValue()),
                new PermanentHasSubtypePredicate(CardSubtype.SALAMANDER)));
    }
}
