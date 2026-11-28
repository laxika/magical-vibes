package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "19")
public class EumidianLifeseed extends Card {

    /**
     * Adagia, Blast Zone, Cascading Cataracts, Contested War Zone, Deserted Temple, Dust Bowl, Kavaron, Mutavault,
     * Nesting Grounds, Plaza of Heroes, Sunken Citadel, Susur Secundi, Terrain Generator and Uthros.
     */
    private static final List<SpellbookCard> SPELLBOOK = List.of(
            new SpellbookCard("EOE", "250"),
            new SpellbookCard("WAR", "244"),
            new SpellbookCard("AKH", "240"),
            new SpellbookCard("MBS", "144"),
            new SpellbookCard("ODY", "320"),
            new SpellbookCard("MMQ", "316"),
            new SpellbookCard("EOE", "255"),
            new SpellbookCard("MOR", "148"),
            new SpellbookCard("C20", "71"),
            new SpellbookCard("DMU", "252"),
            new SpellbookCard("LCI", "285"),
            new SpellbookCard("EOE", "259"),
            new SpellbookCard("NEM", "143"),
            new SpellbookCard("EOE", "260"));

    public EumidianLifeseed() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), DraftFromSpellbookEffect.draftIntoHand(SPELLBOOK));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaOfColorsEffect(
                        ManaColor.COLORS, new ManaRestriction.LandAbilities())),
                "{T}: Add one mana of any color. Spend this mana only to activate abilities of land sources."
        ));
    }
}
