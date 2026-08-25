package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MastercraftRaptor;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "164")
public class SaheelisLattice extends Card {

    public SaheelisLattice() {
        setBackFaceCard(new MastercraftRaptor());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardAndDrawCardEffect(1, 2), "Discard a card to draw two cards?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(
                        new ExileSelfCost(),
                        new CraftMaterialCost(CardSubtype.DINOSAUR),
                        new ReturnSourceFromExileTransformedEffect()),
                "{4}{R}, Exile this artifact, Exile one or more Dinosaurs you control and/or Dinosaur cards from your graveyard: "
                        + "Return this card transformed under its owner's control. Craft only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "MastercraftRaptor";
    }
}
