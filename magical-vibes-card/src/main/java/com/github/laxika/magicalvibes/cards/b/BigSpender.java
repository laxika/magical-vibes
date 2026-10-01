package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "YSNC", collectorNumber = "10")
public class BigSpender extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Stuffed Bear",
            "Daredevil Dragster",
            "Honored Heirloom",
            "Treasure Vault",
            "Gilded Lotus",
            "Heraldic Banner",
            "Key to the City",
            "Prophetic Prism",
            "Filigree Familiar",
            "Golden Egg",
            "Fountain of Renewal",
            "Guild Globe",
            "Zephyr Boots",
            "Arcane Encyclopedia",
            "Diamond Mare");

    public BigSpender() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_BECOME_BLOCKED, CreateTokenEffect.ofTreasureToken(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsArtifactPredicate()),
                        new DraftCardFromSpellbookEffect(SPELLBOOK)
                ),
                "Sacrifice two artifacts: Draft a card from Big Spender's spellbook."
        ));
    }
}
