package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "75")
public class UrzaLordHighArtificer extends Card {

    public UrzaLordHighArtificer() {
        PermanentCount artifactsYouControl = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        CreateTokenEffect constructToken = new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Construct",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.CONSTRUCT),
                Set.of(),
                Set.of(CardType.ARTIFACT),
                false,
                false,
                Map.of(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                        artifactsYouControl, artifactsYouControl, GrantScope.SELF)),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());

        // When Urza enters, create a 0/0 Construct artifact creature token with a dynamic artifact count boost.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, constructToken);

        // Tap an untapped artifact you control: Add {U}.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentIsArtifactPredicate()),
                        new AwardManaEffect(ManaColor.BLUE)
                ),
                "Tap an untapped artifact you control: Add {U}."
        ));

        // {5}: Shuffle your library, then exile the top card. Until end of turn, you may play that card without paying its mana cost.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(
                        new ShuffleLibraryEffect(false),
                        new ExileTopCardMayPlayThisTurnEffect(true)
                ),
                "{5}: Shuffle your library, then exile the top card. Until end of turn, you may play that card without paying its mana cost."
        ));
    }
}
