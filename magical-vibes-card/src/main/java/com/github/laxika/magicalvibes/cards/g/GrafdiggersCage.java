package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardsCantEnterBattlefieldFromGraveyardsAndLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromLibrariesEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DKA", collectorNumber = "149")
public class GrafdiggersCage extends Card {

    public GrafdiggersCage() {
        // Creature cards in graveyards and libraries can't enter the battlefield.
        addEffect(EffectSlot.STATIC, new CardsCantEnterBattlefieldFromGraveyardsAndLibrariesEffect(
                new CardTypePredicate(CardType.CREATURE)));

        // Players can't cast spells from graveyards or libraries.
        addEffect(EffectSlot.STATIC, new PlayersCantCastSpellsFromGraveyardsEffect());
        addEffect(EffectSlot.STATIC, new PlayersCantCastSpellsFromLibrariesEffect());
    }
}
