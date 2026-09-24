package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.ControllerDiscardedCardThisTurn;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "186")
public class Asmoranomardicadaistinaculdacar extends Card {

    public Asmoranomardicadaistinaculdacar() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{B/R}")),
                new ControllerDiscardedCardThisTurn(),
                false));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(new CardNamedPredicate("The Underworld Cookbook")),
                "Search your library for a card named The Underworld Cookbook?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(2,
                                new PermanentHasSubtypePredicate(CardSubtype.FOOD)),
                        new TargetCreatureDealsDamageToSelfEffect(6)),
                "Sacrifice two Foods: Target creature deals 6 damage to itself."
        ));
    }
}
