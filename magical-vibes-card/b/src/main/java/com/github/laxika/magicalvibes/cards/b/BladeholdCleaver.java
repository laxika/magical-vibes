package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YONE", collectorNumber = "18")
public class BladeholdCleaver extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("MBS", "1"),
            new DraftFromSpellbookEffect.SpellbookCard("MBS", "2"),
            new DraftFromSpellbookEffect.SpellbookCard("SOM", "4"),
            new DraftFromSpellbookEffect.SpellbookCard("SOM", "84"),
            new DraftFromSpellbookEffect.SpellbookCard("SOM", "92"),
            new DraftFromSpellbookEffect.SpellbookCard("MBS", "8"),
            new DraftFromSpellbookEffect.SpellbookCard("MBS", "66"),
            new DraftFromSpellbookEffect.SpellbookCard("NPH", "128"),
            new DraftFromSpellbookEffect.SpellbookCard("MBS", "14"),
            new DraftFromSpellbookEffect.SpellbookCard("SOM", "101"),
            new DraftFromSpellbookEffect.SpellbookCard("SOM", "23"),
            new DraftFromSpellbookEffect.SpellbookCard("ONE", "143"),
            new DraftFromSpellbookEffect.SpellbookCard("ONE", "121"),
            new DraftFromSpellbookEffect.SpellbookCard("ONE", "197"),
            new DraftFromSpellbookEffect.SpellbookCard("ONE", "128"));

    public BladeholdCleaver() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LivingWeaponEffect(new CreateTokenEffect(
                        "Rebel", 2, 2, CardColor.RED, List.of(CardSubtype.REBEL), Set.of(), Set.of())));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES,
                new DraftCardFromSpellbookEffect(SPELLBOOK));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
