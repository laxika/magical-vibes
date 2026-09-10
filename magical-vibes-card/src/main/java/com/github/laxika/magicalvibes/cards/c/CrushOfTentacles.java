package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "53")
public class CrushOfTentacles extends Card {

    public CrushOfTentacles() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{3}{U}{U}")),
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()),
                false));
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.allPermanentsMatching(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForAlternateCost(),
                new CreateTokenEffect("Octopus", 8, 8, CardColor.BLUE,
                        List.of(CardSubtype.OCTOPUS), Set.of(), Set.of())));
    }
}
