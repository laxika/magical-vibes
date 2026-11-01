package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.KayaSpiritsJusticeEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "211")
@CardRegistration(set = "MKM", collectorNumber = "335")
public class KayaSpiritsJustice extends Card {

    public KayaSpiritsJustice() {
        // "Whenever one or more creatures you control and/or creature cards in your graveyard are put into exile":
        // one ability of two trigger conditions, triggering once for the event.
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), Triggers.CREATURE,
                        ObjectRelation.YOU_CONTROL)),
                new TriggerCondition(Triggers.exiledFrom(Set.of(Zone.GRAVEYARD), Triggers.NONTOKEN_CREATURE,
                        ObjectRelation.YOU_OWN))),
                TriggerOptions.ONE_OR_MORE, List.of(new EffectRegistration(new KayaSpiritsJusticeEffect()))));

        addActivatedAbility(new ActivatedAbility(
                2,
                List.of(new SurveilEffect(2), new ExileUpToOneCardFromGraveyardEffect(true)),
                "+2: Surveil 2, then exile a card from a graveyard."));

        addActivatedAbility(new ActivatedAbility(
                1,
                List.of(new CreateTokenEffect(1, "Spirit", 1, 1, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.SPIRIT),
                        Set.of(Keyword.FLYING), Set.of())),
                "+1: Create a 1/1 white and black Spirit creature token with flying."));

        ActivatedAbility exileCreatures = new ActivatedAbility(
                false,
                null,
                List.of(new ExileTargetPermanentEffect()),
                "-2: Exile target creature you control. For each other player, exile up to one target creature that player controls.",
                TargetFilters.creatureAnOpponentControls(),
                -2,
                null,
                null,
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls()),
                1,
                99
        ).withMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        addActivatedAbility(exileCreatures);
    }
}
