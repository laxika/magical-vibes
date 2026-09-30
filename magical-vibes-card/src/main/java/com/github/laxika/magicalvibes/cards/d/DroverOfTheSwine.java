package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "17")
public class DroverOfTheSwine extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "First Little Pig", "Second Little Pig", "Third Little Pig");

    public DroverOfTheSwine() {
        CardPredicate boarCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSubtypePredicate(CardSubtype.BOAR)));
        TargetFilter graveyardBoar = new GraveyardCardPredicateTargetFilter(
                boarCreature, GraveyardSearchScope.CONTROLLERS_GRAVEYARD);

        setMultiTargetConstraint(MultiTargetConstraint.DIFFERENT_NAMES);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Conjure a card of your choice from the Three Pigs spellbook onto the battlefield",
                                DraftCardFromSpellbookEffect.toBattlefield(SPELLBOOK, null)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Return up to three target Boar creature cards with different names from your graveyard to the battlefield",
                                List.of(new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                                        boarCreature, 3, false, false)),
                                graveyardBoar, null, 0, 3, false, null)
                ))));
    }
}
