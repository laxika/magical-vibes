package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureTypeWithBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "91")
@CardRegistration(set = "FDN", collectorNumber = "330")
@CardRegistration(set = "FDN", collectorNumber = "392")
@CardRegistration(set = "FDN", collectorNumber = "466")
public class KellanPlanarTrailblazer extends Card {

    public KellanPlanarTrailblazer() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(
                SequenceEffect.of(
                        new ConditionalEffect(new SourceHasSubtype(CardSubtype.SCOUT),
                                new GrantEffectToTargetEffect(
                                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                                        new ExileTopCardMayPlayThisTurnEffect(false)),
                                        Duration.PERMANENT, false, GrantScope.SELF)),
                        BecomeCreatureTypeWithBasePowerToughnessEffect.settingCreatureTypes(
                                null, null, CardSubtype.SCOUT,
                                CardSubtype.HUMAN, CardSubtype.FAERIE, CardSubtype.DETECTIVE)
                )),
                "{1}{R}: If Kellan is a Scout, it becomes a Human Faerie Detective and gains \"Whenever Kellan deals combat damage to a player, exile the top card of your library. You may play that card this turn.\""));

        addActivatedAbility(new ActivatedAbility(false, "{2}{R}", List.of(
                SequenceEffect.of(
                        new ConditionalEffect(new SourceHasSubtype(CardSubtype.DETECTIVE),
                                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF,
                                        Duration.PERMANENT)),
                        BecomeCreatureTypeWithBasePowerToughnessEffect.settingCreatureTypes(
                                3, 2, CardSubtype.DETECTIVE,
                                CardSubtype.HUMAN, CardSubtype.FAERIE, CardSubtype.ROGUE)
                )),
                "{2}{R}: If Kellan is a Detective, it becomes a 3/2 Human Faerie Rogue and gains double strike."));
    }
}
