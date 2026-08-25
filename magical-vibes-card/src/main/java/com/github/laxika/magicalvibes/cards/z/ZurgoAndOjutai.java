package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOneOfCombatDamageDealersToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MOM", collectorNumber = "258")
public class ZurgoAndOjutai extends Card {

    public ZurgoAndOjutai() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceEnteredBattlefieldThisTurn(),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.DRAGON),
                        SequenceEffect.of(
                                new LookAtTopCardsHandTopBottomEffect(3),
                                new MayEffect(
                                        new ReturnOneOfCombatDamageDealersToHandThenEffect(
                                                null, "one of those Dragons"),
                                        "Return one of those Dragons to its owner's hand?")),
                        false,
                        true));
    }
}
