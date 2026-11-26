package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "117")
public class BreyasApprentice extends Card {

    public BreyasApprentice() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect("Thopter", 1, 1, null,
                        List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING),
                        Set.of(CardType.ARTIFACT)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Exile the top card of your library. Until the end of your next turn, you may play that card.",
                                        new ExileTopCardMayPlayThisTurnEffect(1, false)
                                                .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Target creature gets +2/+0 until end of turn.",
                                        BoostEffect.target(2, 0),
                                        TargetFilters.creature())))
                ),
                "{T}, Sacrifice an artifact: Choose one — Exile the top card of your library. Until the end of your next turn, you may play that card; or target creature gets +2/+0 until end of turn."
        ).withModalChoiceAtActivation());
    }
}
