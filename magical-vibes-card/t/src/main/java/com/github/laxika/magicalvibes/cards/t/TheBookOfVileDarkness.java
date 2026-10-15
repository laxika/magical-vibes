package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithTriggeredAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "91")
public class TheBookOfVileDarkness extends Card {

    public TheBookOfVileDarkness() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new ControllerLostLifeThisTurn(2), CreateTokenEffect.blackZombie(1)));

        PermanentAllOfPredicate eyeOfVecna = artifactNamed("Eye of Vecna");
        PermanentAllOfPredicate handOfVecna = artifactNamed("Hand of Vecna");
        CreateTokenEffect vecna = new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Vecna",
                8,
                8,
                CardColor.BLACK,
                Set.of(),
                List.of(CardSubtype.ZOMBIE, CardSubtype.GOD),
                Set.of(Keyword.INDESTRUCTIBLE),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                true,
                0,
                Set.of(),
                Set.of(CardSupertype.LEGENDARY)
        );

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileSelfCost(true),
                        new ExilePermanentCost(eyeOfVecna, "an artifact named Eye of Vecna", true, false, true),
                        new ExilePermanentCost(handOfVecna, "an artifact named Hand of Vecna", true, false, true),
                        new CreateTokenWithTriggeredAbilitiesOfExiledCardsEffect(vecna)
                ),
                "{T}, Exile The Book of Vile Darkness and artifacts you control named Eye of Vecna and Hand of Vecna: "
                        + "Create Vecna, a legendary 8/8 black Zombie God creature token with indestructible and all "
                        + "triggered abilities of the exiled cards."
        ));
    }

    private static PermanentAllOfPredicate artifactNamed(String name) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNamedPredicate(name)
        ));
    }
}
