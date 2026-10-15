package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "79")
public class TezzeretArtificeMaster extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of your end step, search your library for a permanent card, put it onto "
                    + "the battlefield, then shuffle.";

    public TezzeretArtificeMaster() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect("Thopter", 1, 1, null, List.of(CardSubtype.THOPTER),
                        Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT))),
                "+1: Create a 1/1 colorless Thopter artifact creature token with flying."
        ));

        // 0: the "draw two instead" is encoded as two mutually exclusive conditional draws
        // (Visions of Beyond pattern); Metalcraft is exactly "you control three or more artifacts".
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new ConditionalEffect(new Metalcraft(), new DrawCardEffect(2)),
                        new ConditionalEffect(new NotCondition(new Metalcraft()), new DrawCardEffect(1))),
                "0: Draw a card. If you control three or more artifacts, draw two cards instead."
        ));

        // −9: the emblem's end-step trigger is fired by StepTriggerService on its controller's turn.
        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(EmblemTriggerStep.END_STEP,
                                List.of(new SearchLibraryEffect(new CardIsPermanentPredicate(),
                                        LibrarySearchDestination.BATTLEFIELD)),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "−9: You get an emblem with \"" + EMBLEM_TEXT + "\""
        ));
    }
}
