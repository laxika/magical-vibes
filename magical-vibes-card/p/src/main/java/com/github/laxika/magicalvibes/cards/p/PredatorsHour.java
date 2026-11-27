package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "147")
@CardRegistration(set = "VOC", collectorNumber = "21")
@CardRegistration(set = "VOC", collectorNumber = "59")
public class PredatorsHour extends Card {

    public PredatorsHour() {
        addSpellEffect(new GrantKeywordEffect(Keyword.MENACE, Subjects.otherCreaturesYouControl()));
        addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopCardMayPlayThisTurnEffect(1, false).fromLibraryOf(Subjects.triggeringPlayer())
                        .exiledFaceDown().withManaAsThoughAnyColor().until(Duration.PERMANENT)), Subjects.creaturesYouControl()));
    }
}
