package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesNameWithCardExiledWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "44")
public class Mindreaver extends Card {

    public Mindreaver() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new ExileTopCardsToSourceEffect(3, false, false, LibraryScope.TARGET_PLAYER)),
                new StackEntryTargetsSourcePredicate()
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{U}",
                List.of(new SacrificeSelfCost(), new CounterSpellEffect()),
                "{U}{U}, Sacrifice Mindreaver: Counter target spell with the same name as a card exiled with Mindreaver.",
                new StackEntryPredicateTargetFilter(
                        new StackEntrySharesNameWithCardExiledWithSourcePredicate(),
                        "Target must be a spell with the same name as a card exiled with Mindreaver.")
        ));
    }
}
