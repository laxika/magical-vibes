package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "103")
@CardRegistration(set = "TLE", collectorNumber = "185")
public class DesperatePlea extends Card {

    public DesperatePlea() {
        ReturnCardFromGraveyardEffect returnCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                .targetGraveyard(true)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .requiresPowerAtMostSacrificedPower(true)
                .build();
        DestroyTargetPermanentEffect destroyCreature = new DestroyTargetPermanentEffect();
        TargetFilter creatureCard = new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
        TargetFilter creaturePermanent = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature");

        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(false, true));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to the battlefield if its power is less than or equal to the sacrificed creature's power",
                        returnCreature, creatureCard),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature",
                        destroyCreature, creaturePermanent),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to the battlefield if its power is less than or equal to the sacrificed creature's power and destroy target creature",
                        List.<CardEffect>of(returnCreature, destroyCreature),
                        List.of(creatureCard, creaturePermanent))
        )));
    }
}
