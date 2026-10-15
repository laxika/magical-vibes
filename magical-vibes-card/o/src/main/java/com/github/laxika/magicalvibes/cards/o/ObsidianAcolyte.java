package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "22")
public class ObsidianAcolyte extends Card {

    public ObsidianAcolyte() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new GrantProtectionFromColorUntilEndOfTurnEffect(CardColor.BLACK)),
                "{W}: Target creature gains protection from black until end of turn.",
                TargetFilters.creature()
        ));
    }
}
