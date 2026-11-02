package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "56")
public class ReservoirKraken extends Card {

    public ReservoirKraken() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY), new ConditionalEffect(
                new SourceUntapped(),
                new AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect(fishToken())));
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));
    }

    private static CreateTokenEffect fishToken() {
        return new CreateTokenEffect(
                1,
                "Fish",
                1,
                1,
                CardColor.BLUE,
                List.of(CardSubtype.FISH),
                Set.of(),
                Set.of(),
                List.of(StaticAbility.of(new CantBeBlockedEffect())));
    }
}
