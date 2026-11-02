package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.LookAtCreatureCardsInTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "10")
public class SlayersBounty extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "2"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "23"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "5"),
            new DraftFromSpellbookEffect.SpellbookCard("RNA", "5"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "15"),
            new DraftFromSpellbookEffect.SpellbookCard("MH1", "23"),
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "5"),
            new DraftFromSpellbookEffect.SpellbookCard("AKH", "9"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "18"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "13"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "17"),
            new DraftFromSpellbookEffect.SpellbookCard("RIX", "12"),
            new DraftFromSpellbookEffect.SpellbookCard("MRD", "16"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "31"),
            new DraftFromSpellbookEffect.SpellbookCard("RNA", "24"));

    private static DraftCardFromSpellbookEffect draftFromSpellbook() {
        return new DraftCardFromSpellbookEffect(SPELLBOOK);
    }

    public SlayersBounty() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new LookAtCreatureCardsInTargetPlayerHandEffect());

        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.sacrificed(new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                                ObjectRelation.ANOTHER_YOU_CONTROL)),
                new TriggerCondition(Triggers.sacrificed(ObjectRelation.THIS))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(draftFromSpellbook()))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{2}, Sacrifice Slayer's Bounty: Draw a card."
        ));
    }
}
