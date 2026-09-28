package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CapsizingWave;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "HBG", collectorNumber = "134")
public class SwordCoastSerpent extends Card {

    public SwordCoastSerpent() {
        setBackFaceCard(new CapsizingWave());
        addCastingOption(new AdventureCast("{1}{U}"));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerCastSpellThisTurn(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
    }

    @Override
    public String getBackFaceClassName() {
        return "CapsizingWave";
    }
}
