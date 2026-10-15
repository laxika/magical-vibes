package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SearchForBlex;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "148")
public class BlexVexingPest extends Card {

    public BlexVexingPest() {
        SearchForBlex backFace = new SearchForBlex();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.PEST, CardSubtype.BAT, CardSubtype.INSECT,
                        CardSubtype.SNAKE, CardSubtype.SPIDER))));
        addEffect(EffectSlot.ON_DEATH, new GainLifeEffect(4));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Blex, Vexing Pest", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Search for Blex", backFace.getEffects(EffectSlot.SPELL)))));
    }

    @Override
    public String getBackFaceClassName() {
        return "SearchForBlex";
    }
}
