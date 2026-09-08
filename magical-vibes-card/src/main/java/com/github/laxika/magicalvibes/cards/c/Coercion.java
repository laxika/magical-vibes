package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "118")
@CardRegistration(set = "S99", collectorNumber = "69")
@CardRegistration(set = "TPR", collectorNumber = "86")
@CardRegistration(set = "P02", collectorNumber = "66")
@CardRegistration(set = "PTK", collectorNumber = "70")
@CardRegistration(set = "8ED", collectorNumber = "122")
@CardRegistration(set = "6ED", collectorNumber = "119")
@CardRegistration(set = "VIS", collectorNumber = "54")
@CardRegistration(set = "TMP", collectorNumber = "113")
@CardRegistration(set = "BTD", collectorNumber = "20")
public class Coercion extends Card {

    public Coercion() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(1, List.of(), HandChoiceDestination.DISCARD));
    }
}
