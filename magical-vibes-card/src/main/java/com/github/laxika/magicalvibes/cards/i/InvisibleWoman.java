package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "1")
public class InvisibleWoman extends Card {

    public InvisibleWoman() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControllerCastSpellThisTurn(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new CreateTokenEffect("Wall", 0, 3, null, List.of(CardSubtype.WALL),
                        Set.of(Keyword.DEFENDER, Keyword.REACH), Set.of())));

        PermanentCount creaturesYouControl = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new MayPayManaEffect(
                        "{R}{G}{W}{U}",
                        SequenceEffect.of(
                                new BoostTargetCreatureEffect(creaturesYouControl, new Fixed(0)),
                                new MakeCreatureUnblockableEffect()),
                        "Pay {R}{G}{W}{U} to boost a creature and make it unblockable?"));
    }
}
