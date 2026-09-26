package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnlyEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.LookAtCreatureCardsInTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "10")
public class SlayersBounty extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Bounty Agent",
            "Outflank",
            "Bound in Gold",
            "Bring to Trial",
            "Glass Casket",
            "Reprobation",
            "Collar the Culprit",
            "Compulsory Rest",
            "Expel",
            "Fairgrounds Warden",
            "Iron Verdict",
            "Luminous Bonds",
            "Raise the Alarm",
            "Seal Away",
            "Summary Judgment");

    private static DraftCardFromSpellbookEffect draftFromSpellbook() {
        return new DraftCardFromSpellbookEffect(SPELLBOOK);
    }

    public SlayersBounty() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new LookAtCreatureCardsInTargetPlayerHandEffect());

        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                        draftFromSpellbook()));
        addEffect(EffectSlot.ON_DEATH, new SacrificeOnlyEffect(draftFromSpellbook()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{2}, Sacrifice Slayer's Bounty: Draw a card."
        ));
    }
}
