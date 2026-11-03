package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RingTemptsYouEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "36")
@CardRegistration(set = "HOC", collectorNumber = "76")
public class SauronTheDarkLord extends Card {

    public SauronTheDarkLord() {
        PermanentPredicate legendaryArtifactOrCreature = new PermanentAllOfPredicate(List.of(
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()))));
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessSacrificesEffect(
                        legendaryArtifactOrCreature,
                        "legendary artifact or legendary creature"));

        addTrigger(Triggers.opponentCastsSpell().anySpell(), new AmassGoblinsEffect(1, CardSubtype.ORC));

        PermanentPredicate army = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ARMY)));
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(army), new RingTemptsYouEffect());

        CardEffect discardAndDraw = new DiscardOwnHandThenDrawEffect(new Fixed(4));
        addTrigger(Triggers.ringTemptsYou(),
                new MayEffect(discardAndDraw, "Discard your hand and draw four cards?"));
    }
}
