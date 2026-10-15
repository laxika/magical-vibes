package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "59")
public class KeyToTheArchive extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Approach of the Second Sun",
            "Claim the Firstborn",
            "Counterspell",
            "Day of Judgment",
            "Demonic Tutor",
            "Despark",
            "Doom Blade",
            "Electrolyze",
            "Growth Spiral",
            "Krosan Grip",
            "Lightning Bolt",
            "Lightning Helix",
            "Putrefy",
            "Regrowth",
            "Time Warp");

    public KeyToTheArchive() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new DraftCardFromSpellbookEffect(SPELLBOOK),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER)));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(2, true)),
                "{T}: Add two mana in any combination of colors."));
    }
}
