package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndLinkToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ExileArtifactsWithTotalManaValueCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "249")
public class MechtitanCore extends Card {

    public MechtitanCore() {
        Map<EffectSlot, CardEffect> tokenEffects = Map.of(
                EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ReturnAllCardsExiledWithSourceEffect(false, null, false, Set.of(), true, true)
        );
        CreateTokenEffect mechtitan = new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Mechtitan",
                10,
                10,
                null,
                Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN),
                List.of(CardSubtype.CONSTRUCT),
                Set.of(Keyword.FLYING, Keyword.VIGILANCE, Keyword.TRAMPLE,
                        Keyword.LIFELINK, Keyword.HASTE),
                Set.of(CardType.ARTIFACT),
                false,
                false,
                tokenEffects,
                List.of(),
                false,
                false,
                true,
                0,
                Set.of(),
                Set.of(CardSupertype.LEGENDARY)
        );

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(
                        new ExileSelfCost(),
                        new ExileArtifactsWithTotalManaValueCost(
                                4,
                                false,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)
                                )),
                                true
                        ),
                        new CreateTokenAndLinkToSourceEffect(mechtitan, false)
                ),
                "{5}, Exile this Vehicle and four other artifact creatures and/or Vehicles you control: Create Mechtitan, a legendary 10/10 Construct artifact creature token with flying, vigilance, trample, lifelink, and haste that's all colors. When that token leaves the battlefield, return all cards exiled with this Vehicle except this card to the battlefield tapped under their owners' control."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
