package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "29")
public class GiantShark extends Card {

    public GiantShark() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                "an Island"
        ));

        SequenceEffect damagedCombatOpponentBonus = SequenceEffect.of(
                new BoostSelfEffect(2, 0),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)
        );
        addEffect(EffectSlot.ON_BLOCK, new TriggeringPermanentConditionalEffect(
                new PermanentDealtDamageThisTurnPredicate(), damagedCombatOpponentBonus));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentDealtDamageThisTurnPredicate(), damagedCombatOpponentBonus),
                TriggerMode.PER_BLOCKER);

        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
                    if (battlefield == null) return true;
                    return battlefield.stream()
                            .noneMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.ISLAND));
                },
                List.of(new SacrificeSelfEffect()),
                "Giant Shark's state-triggered ability"
        ));
    }
}
