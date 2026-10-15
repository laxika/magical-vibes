package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "19")
@CardRegistration(set = "ACR", collectorNumber = "129")
public class EvieFrye extends Card {

    private static final PermanentPredicate OWN_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentControlledBySourceControllerPredicate()));

    public EvieFrye() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(
                        new Fixed(1),
                        new CardNamedPredicate("Jacob Frye"),
                        LibrarySearchDestination.HAND,
                        LibrarySearchPlayer.TARGET_PLAYER),
                "Put Jacob Frye into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new DrawCardEffect(),
                        new DiscardCardThenEffect(
                                null,
                                new MakeCreatureUnblockableEffect(OWN_CREATURE),
                                "a card",
                                new CardTypePredicate(CardType.CREATURE))),
                "{1}, {T}: Draw a card, then discard a card. When you discard a creature card this way, target creature you control can't be blocked this turn."));
    }
}
