package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.OpponentsAttackedThisTurn;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "66")
@CardRegistration(set = "PIP", collectorNumber = "392")
@CardRegistration(set = "PIP", collectorNumber = "594")
@CardRegistration(set = "PIP", collectorNumber = "920")
public class RoseCutthroatRaider extends Card {

    public RoseCutthroatRaider() {
        addTrigger(Triggers.endOfCombat(),
                new ConditionalEffect(new Raid(), junkToken(new OpponentsAttackedThisTurn())));

        addTrigger(Triggers.sacrificed(new PermanentHasSubtypePredicate(CardSubtype.JUNK), ObjectRelation.YOU_CONTROL),
                new AwardManaEffect(ManaColor.RED));
    }

    private static CreateTokenEffect junkToken(OpponentsAttackedThisTurn amount) {
        return CreateTokenEffect.ofArtifactToken(
                amount,
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
