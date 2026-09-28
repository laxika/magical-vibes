package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayForArtifactsOrCreaturesCantAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "18")
@CardRegistration(set = "PIP", collectorNumber = "367")
@CardRegistration(set = "PIP", collectorNumber = "546")
@CardRegistration(set = "PIP", collectorNumber = "895")
public class Overencumbered extends Card {

    public Overencumbered() {
        setEnchantPlayer(true);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CreateTokenForTargetPlayerEffect(CreateTokenEffect.ofClueToken(1)))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CreateTokenForTargetPlayerEffect(CreateTokenEffect.ofFoodToken(1)))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CreateTokenForTargetPlayerEffect(junkToken()));
        addEffect(EffectSlot.OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED,
                new MayPayForArtifactsOrCreaturesCantAttackThisCombatEffect());
    }

    private static CreateTokenEffect junkToken() {
        return CreateTokenEffect.ofArtifactToken(
                1,
                "Junk",
                List.of(CardSubtype.JUNK),
                List.of(new ActivatedAbility(
                        true,
                        null,
                        List.of(new SacrificeSelfCost(), new ExileTopCardMayPlayThisTurnEffect(false)),
                        "{T}, Sacrifice this token: Exile the top card of your library. You may play that card this turn. "
                                + "Activate only as a sorcery.",
                        ActivationTimingRestriction.SORCERY_SPEED)));
    }
}
