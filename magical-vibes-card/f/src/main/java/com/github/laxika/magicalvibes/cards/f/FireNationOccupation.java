package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "105")
@CardRegistration(set = "TLE", collectorNumber = "187")
public class FireNationOccupation extends Card {

    private static final CreateTokenEffect SOLDIER_TOKEN = new CreateTokenEffect(
            CardType.CREATURE, 1, "Soldier", 2, 2,
            CardColor.RED, Set.of(), List.of(CardSubtype.SOLDIER),
            Set.of(Keyword.FIREBENDING), Set.of(), false, false,
            Map.of(EffectSlot.ON_ATTACK,
                    new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 1)),
            List.of(), false, false, false, 0, Set.of());

    public FireNationOccupation() {
        // When this enchantment enters, create a 2/2 red Soldier creature token with firebending 1.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SOLDIER_TOKEN);

        // Whenever you cast a spell during an opponent's turn, create a 2/2 red Soldier creature
        // token with firebending 1.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(SOLDIER_TOKEN), true));
    }
}
