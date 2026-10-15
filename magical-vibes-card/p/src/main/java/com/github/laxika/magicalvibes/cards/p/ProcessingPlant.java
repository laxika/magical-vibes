package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.PutOpponentOwnedExiledCardIntoGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "380")
@CardRegistration(set = "MB2", collectorNumber = "617")
public class ProcessingPlant extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("ProcessingPlant", new OracleData(
                "Processing Plant",
                CardType.LAND,
                Set.of(),
                null,
                null,
                List.of(),
                List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK),
                Set.of(),
                List.of(CardSubtype.ULAMOGS, CardSubtype.POWER_PLANT),
                "Processing Plant enters the battlefield tapped.\n"
                        + "When Processing Plant enters the battlefield, you may put a card an opponent owns from exile "
                        + "into that player's graveyard. If you do, untap Processing Plant. Otherwise, exile the top card "
                        + "of each opponent's library.\n"
                        + "{T}: Add {W}, {U}, {B}, or {C}.",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public ProcessingPlant() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new PutOpponentOwnedExiledCardIntoGraveyardEffect(),
                new ConditionalEffect(new EventValueAtLeast(1),
                        new UntapPermanentsEffect(TapUntapScope.SELF)),
                ConditionalEffect.unless(new NotCondition(new EventValueAtLeast(1)),
                        new ExileTopCardsOfEachOpponentEffect(1))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(
                        ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.COLORLESS))),
                "{T}: Add {W}, {U}, {B}, or {C}."));
    }
}
