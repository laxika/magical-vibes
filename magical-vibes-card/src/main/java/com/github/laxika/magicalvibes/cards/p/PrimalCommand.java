package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "233")
public class PrimalCommand extends Card {

    public PrimalCommand() {
        // Choose two — each targeting mode declares its own per-mode target filter, so the choose-two
        // unwrap gives each chosen mode its own target slot (in card-text order). Modes 0/2 target a
        // player, mode 1 targets a noncreature permanent, mode 3 is non-targeting.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player gains 7 life",
                        new TargetPlayerGainsLifeEffect(7),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player."
                        )),
                new ChooseOneEffect.ChooseOneOption(
                        "Put target noncreature permanent on top of its owner's library",
                        new PutTargetOnTopOfLibraryEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                                "Target must be a noncreature permanent."
                        )),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player shuffles their graveyard into their library",
                        new ShuffleGraveyardIntoLibraryEffect(true),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player."
                        )),
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a creature card, reveal it, put it into your hand, then shuffle",
                        new SearchLibraryEffect(new CardTypePredicate(CardType.CREATURE)))
        ), 2));
    }
}
