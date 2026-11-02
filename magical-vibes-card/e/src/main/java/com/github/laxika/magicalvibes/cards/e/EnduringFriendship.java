package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SelfHasKeyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsToken;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfSourceCardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YDSK", collectorNumber = "20")
public class EnduringFriendship extends Card {

    public EnduringFriendship() {
        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new AllOf(List.of(
                        new SelfHasKeyword(Keyword.DOUBLE),
                        new NotCondition(new SourceIsToken()))),
                SequenceEffect.of(
                        new ConjureDuplicateOfSourceCardIntoHandEffect("YDSK", "20", Set.of(Keyword.DOUBLE)),
                        new RemoveKeywordEffect(Keyword.DOUBLE, GrantScope.SELF, null, EffectDuration.PERMANENT))));

        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                List.of(new BoostAllOwnCreaturesEffect(1, 1, new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.OTTER),
                        new PermanentIsEnchantmentPredicate()))))));

        addTrigger(Triggers.dies(Triggers.CREATURE, ObjectRelation.THIS),
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false, Set.of(CardType.ENCHANTMENT)));
    }
}
