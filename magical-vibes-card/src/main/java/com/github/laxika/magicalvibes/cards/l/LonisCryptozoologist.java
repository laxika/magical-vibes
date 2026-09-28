package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeXPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "204")
public class LonisCryptozoologist extends Card {

    public LonisCryptozoologist() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                CreateTokenEffect.ofClueToken(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeXPermanentsCost(new PermanentHasSubtypePredicate(CardSubtype.CLUE)),
                        new LookAtTopCardsOfTargetLibraryEffect(
                                new XValue(),
                                TargetLibraryAction.MAY_PUT_NONLAND_PERMANENT_WITH_MANA_VALUE_X_ONTO_BATTLEFIELD)
                ),
                "{T}, Sacrifice X Clues: Target opponent reveals the top X cards of their library. You may "
                        + "put a nonland permanent card with mana value X or less from among them onto the "
                        + "battlefield under your control. That player puts the rest on the bottom of their "
                        + "library in a random order.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
