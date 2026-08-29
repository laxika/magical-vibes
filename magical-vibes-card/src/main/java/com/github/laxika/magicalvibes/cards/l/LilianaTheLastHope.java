package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EMN", collectorNumber = "93")
public class LilianaTheLastHope extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of your end step, create X 2/2 black Zombie creature tokens, where X is two plus the number of Zombies you control.";

    public LilianaTheLastHope() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new BoostTargetCreatureEffect(-2, -1, GrantDuration.UNTIL_YOUR_NEXT_TURN)),
                "+1: Up to one target creature gets -2/-1 until your next turn.",
                null, +1, null, null,
                List.<TargetFilter>of(TargetFilters.creature()), 0, 1));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(SequenceEffect.of(
                        new MillEffect(2, MillRecipient.CONTROLLER),
                        new MayEffect(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.HAND)
                                        .filter(new CardTypePredicate(CardType.CREATURE))
                                        .build(),
                                "Return a creature card from your graveyard to your hand?"))),
                "−2: Mill two cards, then you may return a creature card from your graveyard to your hand."));

        PermanentCount zombies = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.END_STEP,
                                List.of(new CreateTokenEffect(
                                        new Sum(new Fixed(2), zombies), "Zombie", 2, 2,
                                        CardColor.BLACK, List.of(CardSubtype.ZOMBIE), Set.of(), Set.of())),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "−7: You get an emblem with \"" + EMBLEM_TEXT + "\"."));
    }
}
