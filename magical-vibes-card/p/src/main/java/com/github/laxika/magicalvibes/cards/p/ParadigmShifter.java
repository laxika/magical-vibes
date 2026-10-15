package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardFromSpellbookToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YSOS", collectorNumber = "23")
public class ParadigmShifter extends Card {

    private static final List<ConjureCardFromSpellbookToHandEffect.CardPrintingReference> SPELLBOOK = List.of(
            new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("SOS", "30"),
            new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("SOS", "44"),
            new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("SOS", "78"),
            new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("SOS", "149"),
            new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("SOS", "120"));

    public ParadigmShifter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureCardFromSpellbookToHandEffect(SPELLBOOK));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaOfColorsEffect(
                        ManaColor.COLORS,
                        2,
                        new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY)))),
                "{T}: Add two mana in any combination of colors. Spend this mana only to cast instant and sorcery spells."
        ));
    }
}
