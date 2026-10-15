package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Freerunning;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "28")
public class PettyLarceny extends Card {

    public PettyLarceny() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{1}{B}")), new Freerunning(), false));

        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(
                true, null, false, false, 0, null, false, false, false, true));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addEffect(EffectSlot.SPELL,
                        new ExileTopCardsToSourceEffect(2, true, false, LibraryScope.TARGET_OPPONENT, true))
                .addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTreasureToken(1));
    }
}
