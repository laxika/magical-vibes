package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "133")
public class FireNationAttacks extends Card {

    public FireNationAttacks() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, 2, "Soldier", 2, 2,
                CardColor.RED, Set.of(), List.of(CardSubtype.SOLDIER),
                Set.of(Keyword.FIREBENDING), Set.of(), false, false,
                Map.of(EffectSlot.ON_ATTACK,
                        new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 1)),
                List.of(), false, false, false, 0, Set.of()));
        addCastingOption(new FlashbackCast("{8}{R}"));
    }
}
