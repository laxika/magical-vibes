package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithTriggeringSpellPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import static com.github.laxika.magicalvibes.model.effect.Subjects.cardsThisWay;
import static com.github.laxika.magicalvibes.model.effect.Subjects.controllerOf;
import static com.github.laxika.magicalvibes.model.effect.Subjects.exiledWithSource;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggeringSpell;

@CardRegistration(set = "DGM", collectorNumber = "34")
public class PossibilityStorm extends Card {

    public PossibilityStorm() {
        PlayerSubject caster = controllerOf(triggeringSpell());
        CardSharesCardTypeWithTriggeringSpellPredicate sharesAType = new CardSharesCardTypeWithTriggeringSpellPredicate();
        addTrigger(Triggers.playerCastsSpell().castFrom(Zone.HAND), SequenceEffect.of(
                ExileTargetSpellEffect.triggeringSpellWithSource(),
                new ExileCardsFromTopUntilEffect(caster, sharesAType).withSource(),
                new MayCastDiscardedCardWithoutPayingManaCostEffect(cardsThisWay(StepKind.EXILE), sharesAType)
                        .castBy(caster),
                PutCardsIntoLibraryEffect.onBottomInRandomOrder(exiledWithSource())));
    }
}
