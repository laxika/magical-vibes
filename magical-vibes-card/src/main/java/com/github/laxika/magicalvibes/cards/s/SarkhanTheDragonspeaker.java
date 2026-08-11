package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesUntilEndOfTurnEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "119")
public class SarkhanTheDragonspeaker extends Card {

    private static final String DRAW_EMBLEM_TEXT =
            "At the beginning of your draw step, draw two additional cards.";
    private static final String END_EMBLEM_TEXT =
            "At the beginning of your end step, discard your hand.";

    public SarkhanTheDragonspeaker() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new AnimatePermanentsEffect(4, 4, List.of(CardSubtype.DRAGON),
                                Set.of(Keyword.FLYING, Keyword.INDESTRUCTIBLE, Keyword.HASTE), CardColor.RED),
                        new SetCardTypesUntilEndOfTurnEffect(Set.of(CardType.CREATURE), GrantScope.SELF)
                ),
                "+1: Until end of turn, Sarkhan becomes a legendary 4/4 red Dragon creature with flying, indestructible, and haste. (He doesn't lose loyalty while he's not a planeswalker.)"
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DealDamageToTargetCreatureEffect(4)),
                "\u22123: Sarkhan deals 4 damage to target creature."
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(
                                new EmblemStepTriggerEffect(EmblemTriggerStep.DRAW_STEP,
                                        List.of(new DrawCardEffect(2)), DRAW_EMBLEM_TEXT),
                                new EmblemStepTriggerEffect(EmblemTriggerStep.END_STEP,
                                        List.of(new DiscardHandEffect()), END_EMBLEM_TEXT)
                        ),
                        DRAW_EMBLEM_TEXT + " " + END_EMBLEM_TEXT
                )),
                "\u22126: You get an emblem with \"" + DRAW_EMBLEM_TEXT + "\" and \""
                        + END_EMBLEM_TEXT + "\""
        ));
    }
}
