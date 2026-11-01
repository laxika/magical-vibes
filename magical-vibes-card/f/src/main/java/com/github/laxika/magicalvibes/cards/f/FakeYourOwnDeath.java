package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "174")
@CardRegistration(set = "OTJ", collectorNumber = "87")
@CardRegistration(set = "SNC", collectorNumber = "79")
public class FakeYourOwnDeath extends Card {

    public FakeYourOwnDeath() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(2, 0))
                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dies(ObjectRelation.THIS),
                        SequenceEffect.of(
                                new ReturnSourceCardFromGraveyardToBattlefieldEffect(true),
                                CreateTokenEffect.ofTreasureToken(1)))));
    }
}
