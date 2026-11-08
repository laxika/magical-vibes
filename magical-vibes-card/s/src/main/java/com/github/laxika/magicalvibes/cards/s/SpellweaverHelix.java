package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.TriggeringSpellMatches;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsCopyPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesNameWithCardExiledWithSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "247")
public class SpellweaverHelix extends Card {

    public SpellweaverHelix() {
        CardPredicate sorcery = new CardTypePredicate(CardType.SORCERY);
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                ExileCardsFromGraveyardEffect.exactFromSingleGraveyard(2, sorcery, true),
                "You may exile two target sorcery cards from a single graveyard."
        ));
        addTrigger(Triggers.playerCastsSpell()
                        .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryIsCopyPredicate(true))),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringSpellMatches(
                        new StackEntrySharesNameWithCardExiledWithSourcePredicate())),
                new MayEffect(CopyImprintedCardAndMayCastCopyEffect.otherExiledCard(),
                        "You may copy the other exiled card and cast it without paying its mana cost?"));
    }
}
