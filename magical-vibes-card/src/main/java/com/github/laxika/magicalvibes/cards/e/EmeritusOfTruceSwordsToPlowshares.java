package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreCreatures;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

/**
 * Emeritus of Truce // Swords to Plowshares (SOS 13).
 * <p>
 * Front face: When this creature enters, target player creates an Inkling token. Then if an
 * opponent controls more creatures than you, this creature becomes prepared.
 */
@CardRegistration(set = "SOS", collectorNumber = "13")
public class EmeritusOfTruceSwordsToPlowshares extends Card {

    public EmeritusOfTruceSwordsToPlowshares() {
        setBackFaceCard(new SwordsToPlowshares());

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect(1, "Inkling", 1, 1,
                        CardColor.WHITE, Set.of(CardColor.WHITE, CardColor.BLACK),
                        List.of(CardSubtype.INKLING), Set.of(Keyword.FLYING), Set.of())
        ));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new OpponentControlsMoreCreatures(1), new BecomePreparedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "SwordsToPlowshares";
    }
}
