package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "36")
@CardRegistration(set = "TDC", collectorNumber = "76")
@CardRegistration(set = "FDC", collectorNumber = "170")
public class ParapetThrasher extends Card {

    public ParapetThrasher() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.DRAGON)),
                TriggerOptions.ONE_OR_MORE,
                ChooseOneEffect.notYetChosenThisTurn(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Destroy target artifact that opponent controls",
                                new DestroyTargetPermanentEffect(),
                                new PermanentPredicateTargetFilter(
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsArtifactPredicate(),
                                                new PermanentControlledByDefendingPlayerPredicate())),
                                        "Target must be an artifact that opponent controls")),
                        new ChooseOneEffect.ChooseOneOption(
                                "This creature deals 4 damage to each other opponent",
                                new DealDamageToPlayersEffect(4, DamageRecipient.EACH_OTHER_OPPONENT)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Exile the top card of your library. You may play it this turn",
                                new ExileTopCardMayPlayThisTurnEffect(false)))));
    }
}
