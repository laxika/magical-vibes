package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LendAHam;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "23")
public class PorcinePortent extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "First Little Pig", "Second Little Pig", "Third Little Pig");

    public PorcinePortent() {
        setBackFaceCard(new LendAHam());
        addCastingOption(new AdventureCast("{2}{B}"));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                DraftCardFromSpellbookEffect.toBattlefield(SPELLBOOK, null));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.BOAR)));
    }

    @Override
    public String getBackFaceClassName() {
        return "LendAHam";
    }
}
