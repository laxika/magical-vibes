package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringArtifactControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BRC", collectorNumber = "13")
@CardRegistration(set = "BRC", collectorNumber = "60")
public class FaridEnterprisingSalvager extends Card {

    public FaridEnterprisingSalvager() {
        addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringArtifactControllerConditionalEffect(scrapToken(), false, true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact"),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put a +1/+1 counter on Farid. It gains menace until end of turn.",
                                        List.of(
                                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                                new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF))),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Goad target creature",
                                        new GoadTargetCreatureUntilNextTurnEffect(),
                                        TargetFilters.creature()),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Discard a card, then draw a card",
                                        new DiscardAndDrawCardEffect())
                        ))
                ),
                "{1}{R}, Sacrifice an artifact: Choose one — Put a +1/+1 counter on Farid. It gains menace until end of turn; goad target creature; or discard a card, then draw a card."
        ).withModalChoiceAtActivation());
    }

    private static CreateTokenEffect scrapToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Scrap", List.of(), List.of(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                        "{T}, Sacrifice this artifact: Add one mana of any color."
                )
        ));
    }
}
