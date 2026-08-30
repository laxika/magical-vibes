package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DeadlyVanity;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "157")
public class SelflessGlyphweaver extends Card {

    public SelflessGlyphweaver() {
        DeadlyVanity backFace = new DeadlyVanity();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileSelfCost(),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES)
                ),
                "Exile Selfless Glyphweaver: Creatures you control gain indestructible until end of turn."
        ));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Selfless Glyphweaver", List.of()),
                new ChooseOneEffect.ChooseOneOption("Deadly Vanity", backFace.getEffects(EffectSlot.SPELL))
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "DeadlyVanity";
    }
}
