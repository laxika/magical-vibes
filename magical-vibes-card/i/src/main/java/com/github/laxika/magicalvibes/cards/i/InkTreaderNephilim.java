package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.TriggeringSpellMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySinglePermanentOrPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

import static com.github.laxika.magicalvibes.model.effect.Subjects.allCreatures;
import static com.github.laxika.magicalvibes.model.effect.Subjects.except;
import static com.github.laxika.magicalvibes.model.effect.Subjects.source;
import static com.github.laxika.magicalvibes.model.effect.Subjects.you;

@CardRegistration(set = "GPT", collectorNumber = "117")
public class InkTreaderNephilim extends Card {

    public InkTreaderNephilim() {
        StackEntryPredicate targetsOnlyThisCreature = new StackEntryAllOfPredicate(List.of(
                new StackEntryTargetsOnlySinglePermanentOrPlayerPredicate(),
                new StackEntryTargetsSourcePredicate()));
        // The intervening "if" ("if that spell targets only this creature") is checked as the spell is cast and again
        // as the ability resolves (CR 603.4).
        addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(targetsOnlyThisCreature),
                new ConditionalEffect(new TriggeringSpellMatches(targetsOnlyThisCreature),
                        new CopyTriggeringSpellEffect().copiedBy(you()).targetingEach(except(allCreatures(), source()))));
    }
}
