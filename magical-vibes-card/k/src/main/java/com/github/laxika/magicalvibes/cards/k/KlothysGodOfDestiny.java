package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorsAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;


@CardRegistration(set = "THB", collectorNumber = "220")
@CardRegistration(set = "THB", collectorNumber = "268")
public class KlothysGodOfDestiny extends Card {

    public KlothysGodOfDestiny() {
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new DevotionToColorsAtLeast(
                        Set.of(ManaColor.RED, ManaColor.GREEN), 7)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), Subjects.source())));
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                SequenceEffect.of(
                        ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false),
                        new ConditionalEffect(
                                HappenedThisWay.toACard(StepKind.EXILE, new CardTypePredicate(CardType.LAND)),
                                new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN))),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE,
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                                SequenceEffect.of(
                                        new GainLifeEffect(2),
                                        new DealDamageToPlayersEffect(2, Subjects.eachOpponent())))));
    }
}
