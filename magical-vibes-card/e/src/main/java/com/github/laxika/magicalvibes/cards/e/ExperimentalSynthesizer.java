package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "138")
public class ExperimentalSynthesizer extends Card {

    public ExperimentalSynthesizer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTopCardMayPlayThisTurnEffect(false));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new ExileTopCardMayPlayThisTurnEffect(false));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                "Samurai",
                                2,
                                2,
                                CardColor.WHITE,
                                List.of(CardSubtype.SAMURAI),
                                Set.of(Keyword.VIGILANCE),
                                Set.of()
                        )
                ),
                "{2}{R}, Sacrifice this artifact: Create a 2/2 white Samurai creature token with vigilance. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
