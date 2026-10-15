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

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YONE", collectorNumber = "18")
public class BladeholdCleaver extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Accorder Paladin",
            "Ardent Recruit",
            "Auriok Sunchaser",
            "Blade-Tribe Berserkers",
            "Goblin Gaveleer",
            "Hero of Bladehold",
            "Hero of Oxid Ridge",
            "Jor Kadeen, the Prevailer",
            "Mirran Crusader",
            "Oxidda Scrapmelter",
            "Sunspear Shikari",
            "Oxidda Finisher",
            "Barbed Batterfist",
            "Bladehold War-Whip",
            "Dragonwing Glider");

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
