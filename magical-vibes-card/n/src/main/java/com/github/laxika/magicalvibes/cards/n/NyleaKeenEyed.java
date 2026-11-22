package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorAtLeast;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCardTypeFromAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "185")
@CardRegistration(set = "THB", collectorNumber = "266")
public class NyleaKeenEyed extends Card {

    public NyleaKeenEyed() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new DevotionToColorAtLeast(ManaColor.GREEN, 5)),
                new RemoveCardTypeFromAttachedPermanentEffect(CardType.CREATURE, Subjects.source())));
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                creatureCard, 1, CostModificationScope.SELF));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new RevealTopCardOfLibraryEffect(Subjects.you()),
                        new PutCardsIntoHandEffect(Subjects.revealedThisWay(creatureCard)),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, new CardNotPredicate(creatureCard)),
                                new MayEffect(new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay()),
                                "Put the revealed card into your graveyard?"))),
                "{2}{G}: Reveal the top card of your library. If it's a creature card, put it into "
                        + "your hand. Otherwise, you may put it into your graveyard."));
    }
}
