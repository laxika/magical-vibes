package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "352")
@CardRegistration(set = "MB2", collectorNumber = "588")
public class WrennAndOne extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of your precombat main phase, add {G} for each creature you control.";

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("WrennAndOne", new OracleData(
                "Wrenn and One",
                CardType.LAND,
                Set.of(CardType.PLANESWALKER),
                null,
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(CardSubtype.WRENN),
                "+1: Wrenn and One gains \"{T}: Add {G}\" until your next turn.\n"
                        + "\u22121: Create a 1/1 green Squirrel creature token.\n"
                        + "\u22124: You get an emblem with \"" + EMBLEM_TEXT + "\".",
                null,
                null,
                Set.of(),
                1,
                null,
                null));
    }

    public WrennAndOne() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new GrantActivatedAbilityEffect(
                        ManaAbilities.tapFor(ManaColor.GREEN),
                        GrantScope.SELF,
                        null,
                        EffectDuration.UNTIL_YOUR_NEXT_TURN)),
                "+1: Wrenn and One gains \"{T}: Add {G}\" until your next turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new CreateTokenEffect(
                        1, "Squirrel", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SQUIRREL), Set.of(), Set.of())),
                "\u22121: Create a 1/1 green Squirrel creature token."
        ));

        PermanentCount creaturesYouControl = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.PRECOMBAT_MAIN,
                                List.of(new AwardManaEffect(ManaColor.GREEN, creaturesYouControl)),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "\u22124: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
