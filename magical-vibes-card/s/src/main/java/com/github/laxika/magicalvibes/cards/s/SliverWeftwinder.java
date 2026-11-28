package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "25")
public class SliverWeftwinder extends Card {

    /** The Slivers Spellbook, each Sliver by a printing of a core set or an expansion where it has one. */
    private static final List<SpellbookCard> SLIVERS_SPELLBOOK = List.of(
            new SpellbookCard("M15", "129"),
            new SpellbookCard("MH1", "119"),
            new SpellbookCard("M14", "129"),
            new SpellbookCard("M14", "9"),
            new SpellbookCard("MH1", "121"),
            new SpellbookCard("MH1", "195"),
            new SpellbookCard("M15", "50"),
            new SpellbookCard("MH1", "88"),
            new SpellbookCard("MH1", "6"),
            new SpellbookCard("MH1", "9"),
            new SpellbookCard("MH1", "132"),
            new SpellbookCard("MH1", "18"),
            new SpellbookCard("MH1", "207"),
            new SpellbookCard("M15", "102"),
            new SpellbookCard("M14", "184"),
            new SpellbookCard("M14", "189"),
            new SpellbookCard("MH1", "68"),
            new SpellbookCard("M14", "30"),
            new SpellbookCard("M15", "211"),
            new SpellbookCard("MH1", "148"),
            new SpellbookCard("M14", "38"),
            new SpellbookCard("M14", "157"),
            new SpellbookCard("MH1", "183"),
            new SpellbookCard("MH1", "200"));

    public SliverWeftwinder() {
        CardSubtypePredicate sliverCard = new CardSubtypePredicate(CardSubtype.SLIVER);
        PermanentHasSubtypePredicate sliverCreature = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);

        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}"))));
        addStaticEffect(AlternativeCostForSpellsEffect.warp("{3}", sliverCard));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(randomSliverConjure(), new DrawCardEffect(1))),
                Subjects.creaturesYouControl(),
                sliverCreature));
    }

    private static ConjureCardsEffect randomSliverConjure() {
        return ConjureCardsEffect.atRandomFrom(SLIVERS_SPELLBOOK, ConjureDestination.intoTopCardsOfLibraryAtRandom(5));
    }
}
