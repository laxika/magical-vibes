package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YBLB", collectorNumber = "8")
public class TempestTrapper extends Card {

    public TempestTrapper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaOfColorsEffect(
                        ManaColor.COLORS,
                        2,
                        new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY)))),
                "{T}: Add two mana in any combination of colors. Spend this mana only to cast instant or sorcery spells."
        ));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new NthSpellCastTriggerEffect(
                3,
                List.of(
                        new ShuffleLibraryEffect(false),
                        new ExileTopCardMayPlayThisTurnEffect(true)
                )
        ));
    }
}
