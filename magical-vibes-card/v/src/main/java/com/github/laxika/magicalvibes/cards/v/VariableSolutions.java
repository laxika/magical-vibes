package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCreatureWithManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SeekCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "12")
public class VariableSolutions extends Card {

    public VariableSolutions() {
        addSpellEffect(new ConditionalEffect(
                spellXEquals(1), new SeekCardToBattlefieldEffect(CardPredicateUtils.basicLand(), true)));
        addSpellEffect(new ConditionalEffect(
                spellXEquals(2), new SacrificePermanentsEffect(
                        1, new PermanentIsArtifactPredicate(), SacrificeRecipient.EACH_OPPONENT)));
        addSpellEffect(new ConditionalEffect(
                spellXEquals(3), new ConjureRandomCreatureWithManaValueEffect(new XValue(), false)));

        addSpellEffect(new ConditionalEffect(new SpellXAtLeast(4), new GainLifeEffect(2)));
        addSpellEffect(new ConditionalEffect(
                new SpellXAtLeast(4), new SeekCardToBattlefieldEffect(CardPredicateUtils.basicLand(), true)));
        addSpellEffect(new ConditionalEffect(
                new SpellXAtLeast(4), new SacrificePermanentsEffect(
                        1, new PermanentIsArtifactPredicate(), SacrificeRecipient.EACH_OPPONENT)));
        addSpellEffect(new ConditionalEffect(
                new SpellXAtLeast(4), new ConjureRandomCreatureWithManaValueEffect(new XValue(), false)));
    }

    private static AllOf spellXEquals(int value) {
        return new AllOf(List.of(
                new SpellXAtLeast(value),
                new NotCondition(new SpellXAtLeast(value + 1))));
    }
}
