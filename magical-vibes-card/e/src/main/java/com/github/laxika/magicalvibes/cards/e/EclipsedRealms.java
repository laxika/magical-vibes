package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "263")
public class EclipsedRealms extends Card {

    private static final List<CardSubtype> CHOOSABLE_TYPES = List.of(
            CardSubtype.ELEMENTAL,
            CardSubtype.ELF,
            CardSubtype.FAERIE,
            CardSubtype.GIANT,
            CardSubtype.GOBLIN,
            CardSubtype.KITHKIN,
            CardSubtype.MERFOLK,
            CardSubtype.TREEFOLK);

    public EclipsedRealms() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect(CHOOSABLE_TYPES));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.CHOSEN_SUBTYPE_SPELL_OR_ABILITY)),
                "{T}: Add one mana of any color. Spend this mana only to cast a spell of the chosen type or activate an ability of a source of the chosen type."
        ));
    }
}
