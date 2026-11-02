package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "85")
@CardRegistration(set = "FDN", collectorNumber = "328")
@CardRegistration(set = "FDN", collectorNumber = "390")
@CardRegistration(set = "FDN", collectorNumber = "465")
public class Electroduplicate extends Card {

    public Electroduplicate() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(), Set.of(), null, null, Map.of(),
                        false, false, false, false,
                        false, false, null, Set.of(Keyword.HASTE),
                        false, List.of(new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                                new SacrificeSelfEffect())),
                        List.of(), false, false,
                        new Fixed(1), false, Set.of()));
        addCastingOption(new FlashbackCast("{2}{R}{R}"));
    }
}
