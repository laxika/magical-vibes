package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "74")
@CardRegistration(set = "LTC", collectorNumber = "154")
@CardRegistration(set = "LTC", collectorNumber = "484")
public class WakeTheDragon extends Card {

    public WakeTheDragon() {
        addSpellEffect(new CreateTokenEffect(
                CardType.CREATURE, 1, "Dragon", 6, 6,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.RED),
                List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING, Keyword.MENACE), Set.of(),
                false, false,
                List.of(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                                GainControlOfTargetEffect.withTargetPredicate(ControlDuration.PERMANENT,
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsArtifactPredicate(),
                                                new PermanentControlledByTriggeringPlayerPredicate()))))),
                List.of(), false, false, false, 0, Set.of()));
        addCastingOption(new FlashbackCast("{6}{B}{R}"));
    }
}
