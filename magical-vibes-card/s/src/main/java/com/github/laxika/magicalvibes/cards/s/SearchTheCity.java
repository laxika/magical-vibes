package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NoCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithCardExiledWithSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Search the City — {4}{U} enchantment.
 *
 * <p>"When this enchantment enters, exile the top five cards of your library. Whenever you play a
 * card with the same name as one of the exiled cards, you may put one of those cards with that name
 * into its owner's hand. Then if there are no cards exiled with this enchantment, sacrifice it. If
 * you do, take an extra turn after this one."
 *
 * <p>"Play a card" is both halves of playing (the card's ruling), so the ability is written as a cast
 * ability and a land-play ability, each filtered to a card sharing a name with a card exiled with this
 * enchantment; "that name" is read from the stack entry's triggering card as the ability resolves.
 */
@CardRegistration(set = "RTR", collectorNumber = "49")
public class SearchTheCity extends Card {

    public SearchTheCity() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTopCardsToSourceEffect(5, false));
        CardEffect[] playedCardWithExiledName = {
                new MayEffect(PutCardExiledWithSourceIntoHandEffect.withTriggeringCardNameToOwnersHand(),
                        "Put a card with that name exiled with Search the City into its owner's hand?"),
                new ConditionalEffect(new NoCardsExiledWithSource(),
                        new ConditionalEffect(new SourceIsOnBattlefield(),
                                SequenceEffect.of(new SacrificeSelfEffect(), new ControllerExtraTurnEffect(1))))};
        addTrigger(Triggers.youCastSpell().withFilter(new CardSharesNameWithCardExiledWithSourcePredicate()),
                playedCardWithExiledName);
        addTrigger(Triggers.youPlayLand().withFilter(new CardSharesNameWithCardExiledWithSourcePredicate()),
                playedCardWithExiledName);
    }
}
