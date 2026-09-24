package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AdagiaWindsweptBastion;
import com.github.laxika.magicalvibes.cards.b.BlastZone;
import com.github.laxika.magicalvibes.cards.c.CascadingCataracts;
import com.github.laxika.magicalvibes.cards.c.ContestedWarZone;
import com.github.laxika.magicalvibes.cards.d.DesertedTemple;
import com.github.laxika.magicalvibes.cards.d.DustBowl;
import com.github.laxika.magicalvibes.cards.k.KavaronMemorialWorld;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.n.NestingGrounds;
import com.github.laxika.magicalvibes.cards.p.PlazaOfHeroes;
import com.github.laxika.magicalvibes.cards.s.SunkenCitadel;
import com.github.laxika.magicalvibes.cards.s.SusurSecundiVoidAltar;
import com.github.laxika.magicalvibes.cards.t.TerrainGenerator;
import com.github.laxika.magicalvibes.cards.u.UthrosTitanicGodcore;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "19")
public class EumidianLifeseed extends Card {

    public EumidianLifeseed() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DraftFromSpellbookEffect(List.of(
                AdagiaWindsweptBastion::new,
                BlastZone::new,
                CascadingCataracts::new,
                ContestedWarZone::new,
                DesertedTemple::new,
                DustBowl::new,
                KavaronMemorialWorld::new,
                Mutavault::new,
                NestingGrounds::new,
                PlazaOfHeroes::new,
                SunkenCitadel::new,
                SusurSecundiVoidAltar::new,
                TerrainGenerator::new,
                UthrosTitanicGodcore::new)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaOfColorsEffect(
                        ManaColor.COLORS, new ManaRestriction.LandAbilities())),
                "{T}: Add one mana of any color. Spend this mana only to activate abilities of land sources."
        ));
    }
}
