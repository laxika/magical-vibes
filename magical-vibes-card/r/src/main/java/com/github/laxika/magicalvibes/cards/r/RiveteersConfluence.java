package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOrGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "79")
@CardRegistration(set = "NCC", collectorNumber = "179")
public class RiveteersConfluence extends Card {

    public RiveteersConfluence() {
        PermanentPredicate opposingCreatureOrPlaneswalker = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModes(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "You draw a card and you lose 1 life",
                        SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))),
                new ChooseOneEffect.ChooseOneOption(
                        "Riveteers Confluence deals 1 damage to each creature and planeswalker you don't control",
                        new MassDamageEffect(1, false, false, true, opposingCreatureOrPlaneswalker)),
                new ChooseOneEffect.ChooseOneOption(
                        "You may put a land card from your hand or graveyard onto the battlefield tapped",
                        new PutUpToCardsFromHandOrGraveyardOntoBattlefieldEffect(
                                new CardTypePredicate(CardType.LAND), "land", 1))
        ), 3));
    }
}
