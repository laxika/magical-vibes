package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "161")
public class PoeticIngenuity extends Card {

    public PoeticIngenuity() {
        PermanentAllOfPredicate attackingDinosaur = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(
                        new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)),
                        CreateTokenEffect.ofTreasureToken(
                                new PermanentCount(attackingDinosaur, CountScope.CONTROLLER))));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new OncePerTurnTriggerEffect(
                new SpellCastTriggerEffect(
                        new CardTypePredicate(CardType.ARTIFACT),
                        List.of(new CreateTokenEffect(
                                "Dinosaur", 3, 1, CardColor.RED,
                                List.of(CardSubtype.DINOSAUR), Set.of(), Set.of())))));
    }
}
