package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesNameWithCardExiledWithSourcePredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "137")
public class JestersScepter extends Card {

    public JestersScepter() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTopCardsToSourceEffect(5, true, false, LibraryScope.TARGET_PLAYER));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new PutCardExiledWithSourceIntoGraveyardCost(), new CounterSpellEffect()),
                "{2}, {T}, Put a card exiled with this artifact into its owner's graveyard: Counter target spell if it has the same name as that card.",
                new StackEntryPredicateTargetFilter(
                        new StackEntrySharesNameWithCardExiledWithSourcePredicate(),
                        "Target must be a spell with the same name as a card exiled with Jester's Scepter."
                )
        ));
    }
}
