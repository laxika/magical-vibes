package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "202")
public class SorinSolemnVisitor extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of each opponent's upkeep, that player sacrifices a creature of their choice.";

    public SorinSolemnVisitor() {
        // +1: Until your next turn, creatures you control get +1/+0 and gain lifelink.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new BoostAllOwnCreaturesEffect(1, 0, GrantDuration.UNTIL_YOUR_NEXT_TURN),
                        new GrantKeywordEffect(Set.of(Keyword.LIFELINK), GrantScope.OWN_CREATURES,
                                GrantDuration.UNTIL_YOUR_NEXT_TURN)
                ),
                "+1: Until your next turn, creatures you control get +1/+0 and gain lifelink."
        ));

        // −2: Create a 2/2 black Vampire creature token with flying.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect("Vampire", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.VAMPIRE), Set.of(Keyword.FLYING), Set.of())),
                "−2: Create a 2/2 black Vampire creature token with flying."
        ));

        // −6: You get an emblem with "At the beginning of each opponent's upkeep, that player
        // sacrifices a creature of their choice."
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.OPPONENT_UPKEEP,
                                List.of(new SacrificePermanentsEffect(
                                        1, new PermanentIsCreaturePredicate(), SacrificeRecipient.ACTIVE_PLAYER)),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "−6: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
