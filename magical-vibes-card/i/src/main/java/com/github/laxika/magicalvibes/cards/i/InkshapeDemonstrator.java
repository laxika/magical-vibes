package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "21")
public class InkshapeDemonstrator extends Card {

    public InkshapeDemonstrator() {
        // Ward {2} (Whenever this creature becomes the target of a spell or ability an opponent
        // controls, counter it unless that player pays {2}.)
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));

        // Repartee — Whenever you cast an instant or sorcery spell that targets a creature,
        // this creature gets +1/+0 and gains lifelink until end of turn.
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate())),
                new BoostSelfEffect(1, 0),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF));
    }
}
